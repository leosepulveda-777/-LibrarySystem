package com.library.service.impl;

import com.library.config.LibraryProperties;
import com.library.dto.request.ReservaRequest;
import com.library.dto.response.PrestamoResponse;
import com.library.dto.response.ReservaResponse;
import com.library.entity.*;
import com.library.enums.*;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaService {

    private final ReservaRepository    reservaRepository;
    private final LectorRepository     lectorRepository;
    private final PrestamoRepository   prestamoRepository;
    private final EjemplarRepository   ejemplarRepository;
    private final MultaRepository      multaRepository;
    private final UsuarioRepository    usuarioRepository;
    private final LibraryProperties    props;
    private final CatalogoService      catalogoService;
    private final NotificationService  notificationService;

    // ─────────────────────────────────────────────
    //  LISTAR
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ReservaResponse> listarTodas(String estado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaReserva").descending());
        if (estado != null && !estado.isBlank()) {
            return reservaRepository
                    .findByEstado(EstadoReserva.valueOf(estado.toUpperCase()), pageable)
                    .map(this::toResponse);
        }
        return reservaRepository.findAll(pageable).map(this::toResponse);
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

    // ─────────────────────────────────────────────
    //  CREAR
    // ─────────────────────────────────────────────

    public ReservaResponse reservar(Long lectorId, ReservaRequest req) {
        Lector lector = lectorRepository.findById(lectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Lector", lectorId));

        if (!lector.isActivo())
            throw new BusinessException("El lector está inactivo");

        Libro libro = catalogoService.findLibro(req.getLibroId());

        if (prestamoRepository.existsByLectorIdAndLibroIdAndEstado(
                lectorId, req.getLibroId(), EstadoPrestamo.ACTIVO))
            throw new BusinessException("No puedes reservar un libro que ya tienes en préstamo activo");

        if (reservaRepository.existsByLectorIdAndLibroIdAndEstadoIn(
                lectorId, req.getLibroId(), List.of(EstadoReserva.PENDIENTE, EstadoReserva.DISPONIBLE)))
            throw new BusinessException("Ya tienes una reserva activa para este libro");

        long reservasActivas = reservaRepository.countByLectorIdAndEstadoIn(
                lectorId, List.of(EstadoReserva.PENDIENTE, EstadoReserva.DISPONIBLE));
        if (reservasActivas >= props.getMaxActiveReservations())
            throw new BusinessException("Has alcanzado el límite de reservas activas (" + props.getMaxActiveReservations() + ")");

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

    // ─────────────────────────────────────────────
    //  CONFIRMAR ENTREGA MANUAL (sin préstamo)
    // ─────────────────────────────────────────────

    public ReservaResponse confirmarEntrega(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        if (reserva.getEstado() != EstadoReserva.DISPONIBLE)
            throw new BusinessException("Solo se pueden confirmar reservas en estado DISPONIBLE");

        reserva.setEstado(EstadoReserva.COMPLETADA);
        reserva = reservaRepository.save(reserva);
        log.info("Reserva {} confirmada manualmente como entregada", reservaId);
        return toResponse(reserva);
    }

    // ─────────────────────────────────────────────
    //  CANCELAR
    // ─────────────────────────────────────────────

    public ReservaResponse cancelar(Long reservaId, Long lectorId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        if (!reserva.getLector().getId().equals(lectorId))
            throw new BusinessException("No tienes permiso para cancelar esta reserva");

        if (reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.COMPLETADA)
            throw new BusinessException("La reserva ya está " + reserva.getEstado().name().toLowerCase());

        reserva.setEstado(EstadoReserva.CANCELADA);
        reserva = reservaRepository.save(reserva);
        reordenarCola(reserva.getLibro().getId());

        log.info("Reserva {} cancelada", reservaId);
        return toResponse(reserva);
    }

    // ─────────────────────────────────────────────
    //  CONVERTIR RESERVA → PRÉSTAMO (flujo principal)
    // ─────────────────────────────────────────────

    /**
     * El bibliotecario pulsa "Aceptar y prestar":
     *  1. Valida lector activo, sin multas, dentro del límite de préstamos físicos.
     *  2. Busca el primer ejemplar disponible del libro reservado.
     *  3. Crea el préstamo físico y marca el ejemplar como PRESTADO.
     *  4. Marca la reserva como COMPLETADA.
     *  5. Notifica al lector.
     *
     * Se implementa aquí (sin delegar a PrestamoService) para evitar dependencia circular.
     */
    public PrestamoResponse convertirAPrestamo(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva", reservaId));

        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.DISPONIBLE) {
            throw new BusinessException(
                    "Solo se pueden convertir reservas en estado PENDIENTE o DISPONIBLE (actual: "
                            + reserva.getEstado() + ")");
        }

        Lector lector = reserva.getLector();

        if (!lector.isActivo())
            throw new BusinessException("El lector está inactivo");

        // Sin multas pendientes
        if (!multaRepository.findByLectorIdAndEstadoIn(
                lector.getId(), List.of(EstadoMulta.PENDIENTE, EstadoMulta.PARCIALMENTE_PAGADA)).isEmpty()) {
            throw new BusinessException("El lector tiene multas pendientes. Debe regularizarlas primero.");
        }

        // Dentro del límite de préstamos físicos
        long fisicosActivos = prestamoRepository
                .findByLectorId(lector.getId(), PageRequest.of(0, 100)).stream()
                .filter(p -> !p.isEsDigital()
                        && (p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.RENOVADO))
                .count();
        if (fisicosActivos >= props.getMaxActiveLoansPhysical()) {
            throw new BusinessException("El lector alcanzó el límite de préstamos físicos activos ("
                    + props.getMaxActiveLoansPhysical() + ")");
        }

        // Ejemplar disponible
        Ejemplar ejemplar = ejemplarRepository
                .findFirstDisponibleByLibroId(reserva.getLibro().getId())
                .orElseThrow(() -> new BusinessException(
                        "No hay ejemplares físicos disponibles para \"" + reserva.getLibro().getTitulo() + "\""));

        ejemplar.setEstado(EstadoEjemplar.PRESTADO);
        ejemplarRepository.save(ejemplar);

        Prestamo prestamo = Prestamo.builder()
                .lector(lector)
                .ejemplar(ejemplar)
                .libro(reserva.getLibro())
                .fechaDevolucionEsperada(LocalDateTime.now().plusDays(props.getLoanDaysPhysical()))
                .esDigital(false)
                .observaciones("Generado desde reserva #" + reservaId)
                .bibliotecario(getBibliotecarioActual())
                .build();

        prestamo = prestamoRepository.save(prestamo);

        reserva.setEstado(EstadoReserva.COMPLETADA);
        reservaRepository.save(reserva);

        notificationService.prestamoConfirmado(
                lector.getUsuario().getId(),
                lector.getUsuario().getEmail(),
                reserva.getLibro().getTitulo());

        log.info("Reserva {} → Préstamo {} (lector={}, libro={})",
                reservaId, prestamo.getId(),
                lector.getNumeroCarnet(), reserva.getLibro().getTitulo());

        return buildPrestamoResponse(prestamo);
    }

    // ─────────────────────────────────────────────
    //  PRIVADOS
    // ─────────────────────────────────────────────

    private void reordenarCola(Long libroId) {
        List<Reserva> cola = reservaRepository.findColaByLibroId(libroId);
        for (int i = 0; i < cola.size(); i++) {
            cola.get(i).setPosicionCola(i + 1);
            reservaRepository.save(cola.get(i));
        }
    }

    private Usuario getBibliotecarioActual() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return usuarioRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Mapeo Prestamo → PrestamoResponse sin depender de PrestamoService. */
    private PrestamoResponse buildPrestamoResponse(Prestamo p) {
        return PrestamoResponse.builder()
                .id(p.getId())
                .lectorId(p.getLector().getId())
                .nombreLector(p.getLector().getUsuario().getNombre() + " " + p.getLector().getUsuario().getApellido())
                .numeroCarnet(p.getLector().getNumeroCarnet())
                .libroId(p.getLibro().getId())
                .tituloLibro(p.getLibro().getTitulo())
                .isbnLibro(p.getLibro().getIsbn())
                .ejemplarId(p.getEjemplar() != null ? p.getEjemplar().getId() : null)
                .codigoEjemplar(p.getEjemplar() != null ? p.getEjemplar().getCodigoInventario() : null)
                .fechaPrestamo(p.getFechaPrestamo())
                .fechaDevolucionEsperada(p.getFechaDevolucionEsperada())
                .estado(p.getEstado())
                .numeroRenovaciones(p.getNumeroRenovaciones())
                .esDigital(false)
                .observaciones(p.getObservaciones())
                .vencido(false)
                .diasRetraso(0)
                .build();
    }

    // ─────────────────────────────────────────────
    //  MAPPER
    // ─────────────────────────────────────────────

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