package com.library.service.impl;

import com.library.config.LibraryProperties;
import com.library.dto.request.ReservaRequest;
import com.library.dto.response.ReservaResponse;
import com.library.entity.*;
import com.library.enums.EstadoReserva;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de gestión de reservas con cola de espera
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final LectorRepository lectorRepository;
    private final LibraryProperties props;
    private final CatalogoService catalogoService;

    /**
     * Crea una reserva para un libro
     */
    public ReservaResponse reservar(Long lectorId, ReservaRequest req) {
        Lector lector = lectorRepository.findById(lectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Lector", lectorId));

        if (!lector.isActivo()) throw new BusinessException("El lector está inactivo");

        Libro libro = catalogoService.findLibro(req.getLibroId());

        // Verificar si ya tiene reserva activa para este libro
        if (reservaRepository.existsByLectorIdAndLibroIdAndEstadoIn(
                lectorId, req.getLibroId(), List.of(EstadoReserva.PENDIENTE, EstadoReserva.DISPONIBLE))) {
            throw new BusinessException("Ya tienes una reserva activa para este libro");
        }

        // Verificar límite de reservas activas
        long reservasActivas = reservaRepository.countByLectorIdAndEstadoIn(
                lectorId, List.of(EstadoReserva.PENDIENTE, EstadoReserva.DISPONIBLE));
        if (reservasActivas >= props.getMaxActiveReservations()) {
            throw new BusinessException("Has alcanzado el límite de reservas activas (" + props.getMaxActiveReservations() + ")");
        }

        // Calcular posición en cola
        int posicion = reservaRepository.findMaxPosicionCola(req.getLibroId()).orElse(0) + 1;

        Reserva reserva = Reserva.builder()
                .lector(lector)
                .libro(libro)
                .posicionCola(posicion)
                .observaciones(req.getObservaciones())
                .build();

        reserva = reservaRepository.save(reserva);
        log.info("Reserva creada: lector={} libro={} posicion={}", lector.getNumeroCarnet(), libro.getTitulo(), posicion);
        return toResponse(reserva);
    }

    /**
     * Cancela una reserva
     */
    public ReservaResponse cancelar(Long reservaId, Long lectorId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        if (!reserva.getLector().getId().equals(lectorId)) {
            throw new BusinessException("No tienes permiso para cancelar esta reserva");
        }
        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new BusinessException("La reserva ya está " + reserva.getEstado().name().toLowerCase());
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva = reservaRepository.save(reserva);

        // Reordenar la cola
        reordenarCola(reserva.getLibro().getId());

        log.info("Reserva {} cancelada", reservaId);
        return toResponse(reserva);
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponse> listarPorLector(Long lectorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaReserva").descending());
        return reservaRepository.findByLectorId(lectorId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> verCola(Long libroId) {
        return reservaRepository.findColaByLibroId(libroId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtenerReserva(Long id) {
        return toResponse(reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", id)));
    }

    private void reordenarCola(Long libroId) {
        List<Reserva> cola = reservaRepository.findColaByLibroId(libroId);
        for (int i = 0; i < cola.size(); i++) {
            cola.get(i).setPosicionCola(i + 1);
            reservaRepository.save(cola.get(i));
        }
    }

    public ReservaResponse toResponse(Reserva r) {
        return ReservaResponse.builder()
                .id(r.getId())
                .lectorId(r.getLector().getId())
                .nombreLector(r.getLector().getUsuario().getNombre() + " " + r.getLector().getUsuario().getApellido())
                .numeroCarnet(r.getLector().getNumeroCarnet())
                .libroId(r.getLibro().getId())
                .tituloLibro(r.getLibro().getTitulo())
                .fechaReserva(r.getFechaReserva())
                .fechaDisponible(r.getFechaDisponible())
                .fechaExpiracion(r.getFechaExpiracion())
                .estado(r.getEstado())
                .posicionCola(r.getPosicionCola())
                .observaciones(r.getObservaciones())
                .build();
    }
}
