package com.library.service.impl;

import com.library.dto.response.*;
import com.library.enums.EstadoEjemplar;
import com.library.enums.EstadoMulta;
import com.library.enums.EstadoPrestamo;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReporteService {

    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final LibroDigitalRepository libroDigitalRepository;
    private final CatalogoService catalogoService;
    private final PrestamoService prestamoService;
    private final MultaService multaService;

    public ReportePrestamosResponse reportePrestamos(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null) desde = LocalDateTime.now().minusMonths(1);
        if (hasta == null) hasta = LocalDateTime.now();

        List<PrestamoResponse> detalle = prestamoRepository
                .findByFechaPrestamoBetween(desde, hasta,
                        PageRequest.of(0, 1000, Sort.by("fechaPrestamo").descending()))
                .map(prestamoService::toResponse)
                .getContent();

        return ReportePrestamosResponse.builder()
                .desde(desde).hasta(hasta)
                .totalPrestamos(detalle.size())
                .prestamosActivos(prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO))
                .prestamosVencidos(prestamoRepository.countByEstado(EstadoPrestamo.VENCIDO))
                .prestamosDevueltos(prestamoRepository.countByEstado(EstadoPrestamo.DEVUELTO))
                .prestamosDigitales(prestamoRepository.countByEsDigital(true))
                .prestamosFisicos(prestamoRepository.countByEsDigital(false))
                .detalle(detalle)
                .build();
    }

    public ReporteMultasResponse reporteMultas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null) desde = LocalDateTime.now().minusMonths(1);
        if (hasta == null) hasta = LocalDateTime.now();

        List<MultaResponse> detalle = multaRepository
                .findByFechaBetween(desde, hasta,
                        PageRequest.of(0, 1000, Sort.by("fechaGeneracion").descending()))
                .map(multaService::toResponse)
                .getContent();

        return ReporteMultasResponse.builder()
                .desde(desde).hasta(hasta)
                .totalMultas(detalle.size())
                .multasPendientes(multaRepository.countByEstado(EstadoMulta.PENDIENTE))
                .multasPagadas(multaRepository.countByEstado(EstadoMulta.PAGADA))
                .multasCondonadas(multaRepository.countByEstado(EstadoMulta.CONDONADA))
                .montoTotal(multaRepository.sumMontoTotal())
                .montoCobrado(multaRepository.sumMontoCobrado())
                .montoPendiente(multaRepository.sumMontoPendiente())
                .detalle(detalle)
                .build();
    }

    public ReporteInventarioResponse reporteInventario() {
        long disponibles = ejemplarRepository.findAll().stream()
                .filter(e -> e.getEstado() == EstadoEjemplar.DISPONIBLE && e.isActivo()).count();
        long prestados = ejemplarRepository.findAll().stream()
                .filter(e -> e.getEstado() == EstadoEjemplar.PRESTADO && e.isActivo()).count();
        long mantenimiento = ejemplarRepository.findAll().stream()
                .filter(e -> e.getEstado() == EstadoEjemplar.EN_MANTENIMIENTO && e.isActivo()).count();

        List<LibroResponse> masPrestados = libroRepository
                .findAll(PageRequest.of(0, 10, Sort.by("titulo"))).stream()
                .map(catalogoService::toLibroResponse)
                .toList();

        return ReporteInventarioResponse.builder()
                .totalLibros(libroRepository.count())
                .totalEjemplares(ejemplarRepository.count())
                .ejemplaresDisponibles(disponibles)
                .ejemplaresPrestados(prestados)
                .ejemplaresEnMantenimiento(mantenimiento)
                .totalLibrosDigitales(libroDigitalRepository.count())
                .librosMasPrestados(masPrestados)
                .build();
    }
}
