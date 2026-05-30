package com.library.service.impl;

import com.library.dto.response.LibroResponse;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.LectorRepository;
import com.library.repository.LibroRepository;
import com.library.repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecomendacionService {

    private final LectorRepository lectorRepository;
    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final CatalogoService catalogoService;

    public List<LibroResponse> recomendar(Long lectorId) {
        lectorRepository.findById(lectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Lector", lectorId));

        List<Long> librosLeidos = prestamoRepository
                .findByLectorId(lectorId, PageRequest.of(0, 50, Sort.by("fechaPrestamo").descending()))
                .map(p -> p.getLibro().getId())
                .getContent();

        if (librosLeidos.isEmpty()) {
            // Sin historial: retornar los 10 más recientes
            return libroRepository.findAll(PageRequest.of(0, 10, Sort.by("id").descending()))
                    .stream()
                    .map(catalogoService::toLibroResponse)
                    .collect(Collectors.toList());
        }

        List<Long> categoriaIds = libroRepository.findAllById(librosLeidos).stream()
                .flatMap(l -> l.getCategorias().stream())
                .map(c -> c.getId())
                .distinct()
                .collect(Collectors.toList());

        if (categoriaIds.isEmpty()) {
            return List.of();
        }

        // FIX: filtra leídos en la query SQL, no en el stream Java
        List<Long> idsParaExcluir = librosLeidos.isEmpty() ? List.of(-1L) : librosLeidos;

        List<LibroResponse> recomendaciones = libroRepository
                .findByCategoriaIdsExcludingLibros(categoriaIds, idsParaExcluir, PageRequest.of(0, 10))
                .stream()
                .distinct()
                .map(catalogoService::toLibroResponse)
                .collect(Collectors.toList());

        log.info("Recomendaciones para lector {}: {} libros", lectorId, recomendaciones.size());
        return recomendaciones;
    }
}