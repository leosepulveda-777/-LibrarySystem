package com.library.service.impl;

import com.library.dto.request.LectorUpdateRequest;
import com.library.dto.response.LectorResponse;
import com.library.entity.Lector;
import com.library.entity.Usuario;
import com.library.enums.EstadoMulta;
import com.library.enums.EstadoPrestamo;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LectorService {

    private final LectorRepository lectorRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;

    @Transactional(readOnly = true)
    public Page<LectorResponse> listar(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaRegistro").descending());
        return lectorRepository.findBySearchTerm(search, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public LectorResponse obtener(Long id) {
        return toResponse(findLector(id));
    }

    @Transactional(readOnly = true)
    public LectorResponse obtenerPorCarnet(String carnet) {
        Lector lector = lectorRepository.findByNumeroCarnet(carnet)
                .orElseThrow(() -> new ResourceNotFoundException("Lector con carnet " + carnet + " no encontrado"));
        return toResponse(lector);
    }

    @Transactional(readOnly = true)
    public LectorResponse obtenerPorUsuarioId(Long usuarioId) {
        Lector lector = lectorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Lector para usuario " + usuarioId + " no encontrado"));
        return toResponse(lector);
    }

    public LectorResponse actualizar(Long id, LectorUpdateRequest req) {
        Lector lector = findLector(id);
        Usuario usuario = lector.getUsuario();

        if (req.getNombre() != null) usuario.setNombre(req.getNombre());
        if (req.getApellido() != null) usuario.setApellido(req.getApellido());
        if (req.getTelefono() != null) usuario.setTelefono(req.getTelefono());
        if (req.getDireccion() != null) lector.setDireccion(req.getDireccion());
        if (req.getFechaNacimiento() != null) lector.setFechaNacimiento(LocalDate.parse(req.getFechaNacimiento()));
        if (req.getActivo() != null) {
            lector.setActivo(req.getActivo());
            usuario.setActivo(req.getActivo());
        }

        return toResponse(lectorRepository.save(lector));
    }

    public LectorResponse toggleActivo(Long id, boolean activo) {
        Lector lector = findLector(id);
        lector.setActivo(activo);
        lector.getUsuario().setActivo(activo);
        log.info("Lector {} {} {}", id, activo ? "activado" : "desactivado", lector.getNumeroCarnet());
        return toResponse(lectorRepository.save(lector));
    }

    // ============ PRIVADOS ============

    private Lector findLector(Long id) {
        return lectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lector", id));
    }

    public LectorResponse toResponse(Lector l) {
        long prestamosActivos = prestamoRepository.countByLectorIdAndEstado(l.getId(), EstadoPrestamo.ACTIVO);
        long multasPendientes = multaRepository
                .findByLectorIdAndEstadoIn(l.getId(), List.of(EstadoMulta.PENDIENTE, EstadoMulta.PARCIALMENTE_PAGADA))
                .size();

        return LectorResponse.builder()
                .id(l.getId())
                .usuarioId(l.getUsuario().getId())
                .nombre(l.getUsuario().getNombre())
                .apellido(l.getUsuario().getApellido())
                .email(l.getUsuario().getEmail())
                .telefono(l.getUsuario().getTelefono())
                .numeroCarnet(l.getNumeroCarnet())
                .fechaNacimiento(l.getFechaNacimiento())
                .direccion(l.getDireccion())
                .fechaRegistro(l.getFechaRegistro())
                .activo(l.isActivo())
                .maxPrestamos(l.getMaxPrestamos())
                .prestamosActivos((int) prestamosActivos)
                .multasPendientes((int) multasPendientes)
                .build();
    }
}
