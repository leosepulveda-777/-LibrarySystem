package com.library.service.impl;

import com.library.config.LibraryProperties;
import com.library.dto.request.PrestamoRequest;
import com.library.dto.response.PrestamoResponse;
import com.library.entity.*;
import com.library.enums.EstadoEjemplar;
import com.library.enums.EstadoMulta;
import com.library.enums.EstadoPrestamo;
import com.library.enums.EstadoReserva;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio de gestión de préstamos físicos y digitales
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LectorRepository lectorRepository;
    private final EjemplarRepository ejemplarRepository;
    private final LibroDigitalRepository libroDigitalRepository;
    private final MultaRepository multaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;
    private final LibraryProperties props;
    private final NotificationService notificationService;
    private final CatalogoService catalogoService;

    /**
     * Registra un préstamo físico
     */
    public PrestamoResponse prestarFisico(PrestamoRequest req) {
        Lector lector = findLector(req.getLectorId());
        validarLectorActivo(lector);
        validarLimitesPrestamo(lector);

        Ejemplar ejemplar;
        if (req.getEjemplarId() != null) {
            ejemplar = ejemplarRepository.findById(req.getEjemplarId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ejemplar", req.getEjemplarId()));
            if (ejemplar.getEstado() != EstadoEjemplar.DISPONIBLE) {
                throw new BusinessException("El ejemplar no está disponible");
            }
        } else {
            ejemplar = ejemplarRepository.findFirstDisponibleByLibroId(req.getLibroId())
                    .orElseThrow(() -> new BusinessException("No hay ejemplares disponibles para este libro"));
        }

        Libro libro = ejemplar.getLibro();
        ejemplar.setEstado(EstadoEjemplar.PRESTADO);
        ejemplarRepository.save(ejemplar);

        // Cancelar reserva del lector si existía
        reservaRepository.findColaByLibroId(libro.getId()).stream()
                .filter(r -> r.getLector().getId().equals(lector.getId())
                        && (r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.DISPONIBLE))
                .findFirst()
                .ifPresent(r -> { r.setEstado(EstadoReserva.COMPLETADA); reservaRepository.save(r); });

        Prestamo prestamo = Prestamo.builder()
                .lector(lector)
                .ejemplar(ejemplar)
                .libro(libro)
                .fechaDevolucionEsperada(LocalDateTime.now().plusDays(props.getLoanDaysPhysical()))
                .esDigital(false)
                .observaciones(req.getObservaciones())
                .bibliotecario(getBibliotecarioActual())
                .build();

        prestamo = prestamoRepository.save(prestamo);
        notificationService.prestamoConfirmado(lector.getUsuario().getId(),
                lector.getUsuario().getEmail(), libro.getTitulo());

        log.info("Préstamo físico creado: lector={} libro={}", lector.getNumeroCarnet(), libro.getTitulo());
        return toResponse(prestamo);
    }

    /**
     * Registra un préstamo digital
     */
    public PrestamoResponse prestarDigital(PrestamoRequest req) {
        Lector lector = findLector(req.getLectorId());
        validarLectorActivo(lector);
        validarLimitesPrestamo(lector);

        if (prestamoRepository.existsByLectorIdAndLibroIdAndEstado(
                lector.getId(), req.getLibroId(), EstadoPrestamo.ACTIVO)) {
            throw new BusinessException("El lector ya tiene un préstamo activo de este libro");
        }

        LibroDigital ld;
        if (req.getLibroDigitalId() != null) {
            ld = libroDigitalRepository.findById(req.getLibroDigitalId())
                    .orElseThrow(() -> new ResourceNotFoundException("LibroDigital", req.getLibroDigitalId()));
        } else {
            ld = libroDigitalRepository.findDisponibleByLibroId(req.getLibroId())
                    .orElseThrow(() -> new BusinessException("No hay copias digitales disponibles"));
        }

        ld.setPrestamosActivos(ld.getPrestamosActivos() + 1);
        libroDigitalRepository.save(ld);

        Libro libro = catalogoService.findLibro(req.getLibroId());
        Prestamo prestamo = Prestamo.builder()
                .lector(lector)
                .libroDigital(ld)
                .libro(libro)
                .fechaDevolucionEsperada(LocalDateTime.now().plusDays(props.getLoanDaysDigital()))
                .esDigital(true)
                .observaciones(req.getObservaciones())
                .bibliotecario(getBibliotecarioActual())
                .build();

        prestamo = prestamoRepository.save(prestamo);
        notificationService.prestamoConfirmado(lector.getUsuario().getId(),
                lector.getUsuario().getEmail(), libro.getTitulo());

        log.info("Préstamo digital creado: lector={} libro={}", lector.getNumeroCarnet(), libro.getTitulo());
        return toResponse(prestamo);
    }

    /**
     * Devuelve un préstamo y genera multa si corresponde
     */
    public PrestamoResponse devolver(Long prestamoId) {
        Prestamo prestamo = findPrestamo(prestamoId);
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO && prestamo.getEstado() != EstadoPrestamo.VENCIDO) {
            throw new BusinessException("El préstamo no está activo");
        }

        LocalDateTime ahora = LocalDateTime.now();
        prestamo.setFechaDevolucionReal(ahora);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        // Liberar recurso
        if (!prestamo.isEsDigital() && prestamo.getEjemplar() != null) {
            Ejemplar ejemplar = prestamo.getEjemplar();
            ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
            ejemplarRepository.save(ejemplar);
            // Notificar próximo en cola
            notificarProximoEnCola(prestamo.getLibro().getId());
        } else if (prestamo.isEsDigital() && prestamo.getLibroDigital() != null) {
            LibroDigital ld = prestamo.getLibroDigital();
            ld.setPrestamosActivos(Math.max(0, ld.getPrestamosActivos() - 1));
            libroDigitalRepository.save(ld);
        }

        // Generar multa si hay retraso
        if (ahora.isAfter(prestamo.getFechaDevolucionEsperada())) {
            long dias = ChronoUnit.DAYS.between(prestamo.getFechaDevolucionEsperada(), ahora);
            BigDecimal monto = BigDecimal.valueOf(dias * props.getFinePerDay());

            Multa multa = Multa.builder()
                    .lector(prestamo.getLector())
                    .prestamo(prestamo)
                    .monto(monto)
                    .diasRetraso((int) dias)
                    .build();
            multaRepository.save(multa);
            prestamo.setEstado(EstadoPrestamo.DEVUELTO);

            notificationService.multaGenerada(prestamo.getLector().getUsuario().getId(),
                    prestamo.getLector().getUsuario().getEmail(),
                    prestamo.getLibro().getTitulo(), monto.toPlainString());
            log.info("Multa generada: {} días, ${}", dias, monto);
        }

        return toResponse(prestamoRepository.save(prestamo));
    }

    /**
     * Renueva un préstamo activo
     */
    public PrestamoResponse renovar(Long prestamoId) {
        Prestamo prestamo = findPrestamo(prestamoId);
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new BusinessException("Solo se pueden renovar préstamos activos");
        }
        if (prestamo.getNumeroRenovaciones() >= props.getMaxRenewals()) {
            throw new BusinessException("Se alcanzó el límite de renovaciones (" + props.getMaxRenewals() + ")");
        }

        // Verificar que no haya reservas pendientes del libro
        long reservasCola = reservaRepository.findColaByLibroId(prestamo.getLibro().getId()).size();
        if (reservasCola > 0 && !prestamo.isEsDigital()) {
            throw new BusinessException("No se puede renovar: hay reservas pendientes para este libro");
        }

        int diasExtra = prestamo.isEsDigital() ? props.getLoanDaysDigital() : props.getLoanDaysPhysical();
        prestamo.setFechaDevolucionEsperada(prestamo.getFechaDevolucionEsperada().plusDays(diasExtra));
        prestamo.setNumeroRenovaciones(prestamo.getNumeroRenovaciones() + 1);
        prestamo.setEstado(EstadoPrestamo.RENOVADO);

        log.info("Préstamo {} renovado. Renovación #{}", prestamoId, prestamo.getNumeroRenovaciones());
        return toResponse(prestamoRepository.save(prestamo));
    }

    @Transactional(readOnly = true)
    public Page<PrestamoResponse> listarPrestamosPorLector(Long lectorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPrestamo").descending());
        return prestamoRepository.findByLectorId(lectorId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PrestamoResponse> listarTodos(String estado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPrestamo").descending());
        if (estado != null) {
            return prestamoRepository.findByEstado(EstadoPrestamo.valueOf(estado), pageable).map(this::toResponse);
        }
        return prestamoRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtenerPrestamo(Long id) {
        return toResponse(findPrestamo(id));
    }

    // ============ PRIVADOS ============

    private void validarLectorActivo(Lector lector) {
        if (!lector.isActivo()) throw new BusinessException("El lector está inactivo");

        List<com.library.entity.Multa> multasPendientes = multaRepository
                .findByLectorIdAndEstadoIn(lector.getId(),
                        List.of(EstadoMulta.PENDIENTE, EstadoMulta.PARCIALMENTE_PAGADA));
        if (!multasPendientes.isEmpty()) {
            throw new BusinessException("El lector tiene multas pendientes. Debe regularizarlas antes de pedir prestamos.");
        }
    }

    private void validarLimitesPrestamo(Lector lector) {
        long activos = prestamoRepository.countByLectorIdAndEstado(lector.getId(), EstadoPrestamo.ACTIVO);
        if (activos >= lector.getMaxPrestamos()) {
            throw new BusinessException("El lector ha alcanzado el límite de préstamos activos (" + lector.getMaxPrestamos() + ")");
        }
    }

    private void notificarProximoEnCola(Long libroId) {
        reservaRepository.findFirstByLibroIdAndEstadoOrderByPosicionColaAsc(libroId, EstadoReserva.PENDIENTE)
                .ifPresent(reserva -> {
                    reserva.setEstado(EstadoReserva.DISPONIBLE);
                    reserva.setFechaDisponible(LocalDateTime.now());
                    reserva.setFechaExpiracion(LocalDateTime.now().plusDays(props.getReservationExpiryDays()));
                    reservaRepository.save(reserva);
                    notificationService.reservaDisponible(
                            reserva.getLector().getUsuario().getId(),
                            reserva.getLector().getUsuario().getEmail(),
                            reserva.getLibro().getTitulo());
                });
    }

    private Lector findLector(Long id) {
        return lectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lector", id));
    }

    private Prestamo findPrestamo(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo", id));
    }

    private Usuario getBibliotecarioActual() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return usuarioRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public PrestamoResponse toResponse(Prestamo p) {
        LocalDateTime ahora = LocalDateTime.now();
        boolean vencido = p.getEstado() == EstadoPrestamo.ACTIVO &&
                          ahora.isAfter(p.getFechaDevolucionEsperada());
        long diasRetraso = vencido ?
                ChronoUnit.DAYS.between(p.getFechaDevolucionEsperada(), ahora) : 0;

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
                .libroDigitalId(p.getLibroDigital() != null ? p.getLibroDigital().getId() : null)
                .fechaPrestamo(p.getFechaPrestamo())
                .fechaDevolucionEsperada(p.getFechaDevolucionEsperada())
                .fechaDevolucionReal(p.getFechaDevolucionReal())
                .estado(p.getEstado())
                .numeroRenovaciones(p.getNumeroRenovaciones())
                .esDigital(p.isEsDigital())
                .observaciones(p.getObservaciones())
                .vencido(vencido)
                .diasRetraso(diasRetraso)
                .build();
    }
}
