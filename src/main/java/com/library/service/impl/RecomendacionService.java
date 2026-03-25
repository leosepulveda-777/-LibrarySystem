package com.library.service.impl;

import com.library.dto.response.LibroResponse;
import com.library.entity.Libro;
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

/**
 * Microservicio simulado de recomendaciones.
 * Algoritmo: recomienda libros de las mismas categorías que el lector ha prestado.
 */
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

        // Obtener IDs de libros ya prestados por el lector
        List<Long> librosLeidos = prestamoRepository
                .findByLectorId(lectorId, PageRequest.of(0, 50, Sort.by("fechaPrestamo").descending()))
                .map(p -> p.getLibro().getId())
                .getContent();

        if (librosLeidos.isEmpty()) {
            // Sin historial: retornar los libros más recientes
            return libroRepository.findAll(PageRequest.of(0, 10, Sort.by("createdAt").descending()))
                    .stream()
                    .map(catalogoService::toLibroResponse)
                    .collect(Collectors.toList());
        }

        // Obtener categorías de los libros leídos
        List<Long> categoriaIds = libroRepository.findAllById(librosLeidos).stream()
                .flatMap(l -> l.getCategorias().stream())
                .map(c -> c.getId())
                .distinct()
                .collect(Collectors.toList());

        if (categoriaIds.isEmpty()) {
            return List.of();
        }

        // Buscar libros de esas categorías que NO ha leído, máximo 10
        List<LibroResponse> recomendaciones = libroRepository
                .findByCategoriaIds(categoriaIds, PageRequest.of(0, 30))
                .stream()
                .filter(l -> !librosLeidos.contains(l.getId()))
                .distinct()
                .limit(10)
                .map(catalogoService::toLibroResponse)
                .collect(Collectors.toList());

        log.info("Recomendaciones para lector {}: {} libros", lectorId, recomendaciones.size());
        return recomendaciones;
    }
}
