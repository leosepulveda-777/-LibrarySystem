package com.library.scheduler;

import com.library.entity.*;
import com.library.enums.EstadoMulta;
import com.library.enums.EstadoPrestamo;
import com.library.enums.EstadoReserva;
import com.library.repository.*;
import com.library.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Tareas programadas del sistema de biblioteca:
 * - Marcar préstamos vencidos y generar multas
 * - Notificar préstamos próximos a vencer
 * - Expirar reservas vencidas
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LibraryScheduler {

    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final ReservaRepository reservaRepository;
    private final NotificationService notificationService;

    private static final double MULTA_POR_DIA = 500.0;

    /**
     * Cada hora: detecta préstamos vencidos y genera multas automáticamente
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void procesarPrestamosVencidos() {
        log.info("[SCHEDULER] Procesando préstamos vencidos...");
        List<Prestamo> vencidos = prestamoRepository.findVencidos(LocalDateTime.now());

        int procesados = 0;
        for (Prestamo prestamo : vencidos) {
            // Marcar como VENCIDO
            prestamo.setEstado(EstadoPrestamo.VENCIDO);
            prestamoRepository.save(prestamo);

            // Generar multa si no existe ya
            if (!multaRepository.existsByPrestamoId(prestamo.getId())) {
                long dias = ChronoUnit.DAYS.between(prestamo.getFechaDevolucionEsperada(), LocalDateTime.now());
                BigDecimal monto = BigDecimal.valueOf(dias * MULTA_POR_DIA);

                Multa multa = Multa.builder()
                        .lector(prestamo.getLector())
                        .prestamo(prestamo)
                        .monto(monto)
                        .diasRetraso((int) dias)
                        .estado(EstadoMulta.PENDIENTE)
                        .build();
                multaRepository.save(multa);

                notificationService.prestamoVencido(
                        prestamo.getLector().getUsuario().getId(),
                        prestamo.getLector().getUsuario().getEmail(),
                        prestamo.getLibro().getTitulo());

                notificationService.multaGenerada(
                        prestamo.getLector().getUsuario().getId(),
                        prestamo.getLector().getUsuario().getEmail(),
                        prestamo.getLibro().getTitulo(),
                        monto.toPlainString());

                procesados++;
            }
        }

        if (procesados > 0) {
            log.info("[SCHEDULER] {} multas generadas automáticamente", procesados);
        }
    }

    /**
     * Cada día a las 9am: notifica préstamos que vencen en los próximos 2 días
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void notificarPrestamosProximosAVencer() {
        log.info("[SCHEDULER] Notificando préstamos próximos a vencer...");
        LocalDateTime desde = LocalDateTime.now();
        LocalDateTime hasta = LocalDateTime.now().plusDays(2);

        List<Prestamo> porVencer = prestamoRepository.findPorVencer(desde, hasta);
        for (Prestamo prestamo : porVencer) {
            long diasRestantes = ChronoUnit.DAYS.between(LocalDateTime.now(), prestamo.getFechaDevolucionEsperada());
            notificationService.prestamoPorVencer(
                    prestamo.getLector().getUsuario().getId(),
                    prestamo.getLector().getUsuario().getEmail(),
                    prestamo.getLibro().getTitulo(),
                    diasRestantes);
        }

        log.info("[SCHEDULER] {} notificaciones de vencimiento enviadas", porVencer.size());
    }

    /**
     * Cada hora: expira reservas que pasaron su fecha límite
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void expirarReservas() {
        log.info("[SCHEDULER] Procesando reservas expiradas...");
        List<Reserva> expiradas = reservaRepository.findExpiradas(LocalDateTime.now());

        for (Reserva reserva : expiradas) {
            reserva.setEstado(EstadoReserva.EXPIRADA);
            reservaRepository.save(reserva);
            log.info("[SCHEDULER] Reserva {} expirada - lector={} libro={}",
                    reserva.getId(),
                    reserva.getLector().getNumeroCarnet(),
                    reserva.getLibro().getTitulo());
        }

        if (!expiradas.isEmpty()) {
            log.info("[SCHEDULER] {} reservas expiradas", expiradas.size());
        }
    }
}
