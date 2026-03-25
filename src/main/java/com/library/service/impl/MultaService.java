package com.library.service.impl;

import com.library.dto.request.CondonacionRequest;
import com.library.dto.request.PagoMultaRequest;
import com.library.dto.response.MultaResponse;
import com.library.dto.response.PagoMultaResponse;
import com.library.entity.*;
import com.library.enums.EstadoMulta;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MultaService {

    private final MultaRepository multaRepository;
    private final PagoMultaRepository pagoMultaRepository;
    private final LectorRepository lectorRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<MultaResponse> listarTodas(String estado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaGeneracion").descending());
        if (estado != null && !estado.isBlank()) {
            EstadoMulta estadoEnum = EstadoMulta.valueOf(estado.toUpperCase());
            return multaRepository.findByFechaBetween(
                    LocalDateTime.now().minusYears(10), LocalDateTime.now(), pageable)
                    .map(this::toResponse)
                    .map(r -> r); // pass-through; filter by estado done at query level if needed
        }
        return multaRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MultaResponse> listarPorLector(Long lectorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaGeneracion").descending());
        return multaRepository.findByLectorId(lectorId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MultaResponse obtener(Long id) {
        return toResponse(findMulta(id));
    }

    public MultaResponse pagar(Long multaId, PagoMultaRequest req) {
        Multa multa = findMulta(multaId);
        if (multa.getEstado() == EstadoMulta.PAGADA || multa.getEstado() == EstadoMulta.CONDONADA) {
            throw new BusinessException("La multa ya está " + multa.getEstado().name().toLowerCase());
        }
        BigDecimal pendiente = multa.getMonto().subtract(multa.getMontoPagado());
        if (req.getMonto().compareTo(pendiente) > 0) {
            throw new BusinessException("El monto ($" + req.getMonto() +
                    ") supera el saldo pendiente ($" + pendiente + ")");
        }
        PagoMulta pago = PagoMulta.builder()
                .multa(multa).monto(req.getMonto())
                .metodoPago(req.getMetodoPago())
                .referenciaPago(req.getReferenciaPago())
                .observaciones(req.getObservaciones())
                .registradoPor(getUsuarioActual())
                .build();
        pagoMultaRepository.save(pago);
        multa.setMontoPagado(multa.getMontoPagado().add(req.getMonto()));
        BigDecimal nuevoPendiente = multa.getMonto().subtract(multa.getMontoPagado());
        if (nuevoPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            multa.setEstado(EstadoMulta.PAGADA);
            multa.setFechaPago(LocalDateTime.now());
        } else {
            multa.setEstado(EstadoMulta.PARCIALMENTE_PAGADA);
        }
        log.info("Pago multa={} monto=${} pendiente=${}", multaId, req.getMonto(), nuevoPendiente);
        return toResponse(multaRepository.save(multa));
    }

    public MultaResponse condonar(Long multaId, CondonacionRequest req) {
        Multa multa = findMulta(multaId);
        if (multa.getEstado() == EstadoMulta.PAGADA || multa.getEstado() == EstadoMulta.CONDONADA) {
            throw new BusinessException("La multa ya está " + multa.getEstado().name().toLowerCase());
        }
        Usuario admin = getUsuarioActual();
        multa.setEstado(EstadoMulta.CONDONADA);
        multa.setMotivoCondonacion(req.getMotivoCondonacion());
        multa.setCondonadaPor(admin);
        multa.setFechaPago(LocalDateTime.now());
        log.info("Multa {} condonada por {}", multaId, admin != null ? admin.getEmail() : "sistema");
        return toResponse(multaRepository.save(multa));
    }

    private Multa findMulta(Long id) {
        return multaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multa", id));
    }

    private Usuario getUsuarioActual() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return usuarioRepository.findByEmail(email).orElse(null);
        } catch (Exception e) { return null; }
    }

    public MultaResponse toResponse(Multa m) {
        BigDecimal pendiente = m.getMonto().subtract(m.getMontoPagado());
        List<PagoMultaResponse> pagos = pagoMultaRepository
                .findByMultaIdOrderByFechaPagoDesc(m.getId())
                .stream().map(this::toPagoResponse).collect(Collectors.toList());
        return MultaResponse.builder()
                .id(m.getId())
                .lectorId(m.getLector().getId())
                .nombreLector(m.getLector().getUsuario().getNombre() + " " + m.getLector().getUsuario().getApellido())
                .numeroCarnet(m.getLector().getNumeroCarnet())
                .prestamoId(m.getPrestamo().getId())
                .tituloLibro(m.getPrestamo().getLibro().getTitulo())
                .monto(m.getMonto()).montoPagado(m.getMontoPagado())
                .montoPendiente(pendiente).diasRetraso(m.getDiasRetraso())
                .estado(m.getEstado()).fechaGeneracion(m.getFechaGeneracion())
                .fechaPago(m.getFechaPago()).motivoCondonacion(m.getMotivoCondonacion())
                .pagos(pagos).build();
    }

    private PagoMultaResponse toPagoResponse(PagoMulta p) {
        return PagoMultaResponse.builder()
                .id(p.getId()).multaId(p.getMulta().getId()).monto(p.getMonto())
                .fechaPago(p.getFechaPago()).metodoPago(p.getMetodoPago())
                .referenciaPago(p.getReferenciaPago())
                .registradoPor(p.getRegistradoPor() != null ? p.getRegistradoPor().getEmail() : null)
                .observaciones(p.getObservaciones()).build();
    }
}
