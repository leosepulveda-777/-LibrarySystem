package com.library.controller;

import com.library.dto.request.*;
import com.library.dto.response.*;
import com.library.service.impl.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Catálogo", description = "Gestión de libros, autores, categorías, ejemplares y digitales")
public class CatalogoController {

    private final CatalogoService catalogoService;

    // ===== CATEGORÍAS =====

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorías jerárquicas")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarCategorias() {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.listarCategorias()));
    }

    @GetMapping("/categorias/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<ApiResponse<CategoriaResponse>> obtenerCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.obtenerCategoria(id)));
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Crear categoría")
    public ResponseEntity<ApiResponse<CategoriaResponse>> crearCategoria(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(catalogoService.crearCategoria(req)));
    }

    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Actualizar categoría")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizarCategoria(
            @PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.actualizarCategoria(id, req)));
    }

    @DeleteMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar categoría (soft delete)")
    public ResponseEntity<ApiResponse<Void>> eliminarCategoria(@PathVariable Long id) {
        catalogoService.eliminarCategoria(id);
        return ResponseEntity.ok(ApiResponse.ok("Categoría eliminada", null));
    }

    // ===== AUTORES =====

    @GetMapping("/autores")
    @Operation(summary = "Listar autores con búsqueda y paginación")
    public ResponseEntity<ApiResponse<Page<AutorResponse>>> listarAutores(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.listarAutores(search, page, size)));
    }

    @GetMapping("/autores/{id}")
    @Operation(summary = "Obtener autor por ID")
    public ResponseEntity<ApiResponse<AutorResponse>> obtenerAutor(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.obtenerAutor(id)));
    }

    @PostMapping("/autores")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Crear autor")
    public ResponseEntity<ApiResponse<AutorResponse>> crearAutor(@Valid @RequestBody AutorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(catalogoService.crearAutor(req)));
    }

    @PutMapping("/autores/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Actualizar autor")
    public ResponseEntity<ApiResponse<AutorResponse>> actualizarAutor(
            @PathVariable Long id, @Valid @RequestBody AutorRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.actualizarAutor(id, req)));
    }

    @DeleteMapping("/autores/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar autor (soft delete)")
    public ResponseEntity<ApiResponse<Void>> eliminarAutor(@PathVariable Long id) {
        catalogoService.eliminarAutor(id);
        return ResponseEntity.ok(ApiResponse.ok("Autor eliminado", null));
    }

    // ===== LIBROS =====

    @GetMapping("/libros")
    @Operation(summary = "Búsqueda avanzada de libros con filtros y paginación")
    public ResponseEntity<ApiResponse<Page<LibroResponse>>> buscarLibros(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Long autorId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String editorial,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                catalogoService.buscarLibros(titulo, isbn, autorId, categoriaId, editorial, page, size)));
    }

    @GetMapping("/libros/{id}")
    @Operation(summary = "Obtener detalle de libro")
    public ResponseEntity<ApiResponse<LibroResponse>> obtenerLibro(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.obtenerLibro(id)));
    }

    @PostMapping("/libros")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Crear libro")
    public ResponseEntity<ApiResponse<LibroResponse>> crearLibro(@Valid @RequestBody LibroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(catalogoService.crearLibro(req)));
    }

    @PutMapping("/libros/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Actualizar libro")
    public ResponseEntity<ApiResponse<LibroResponse>> actualizarLibro(
            @PathVariable Long id, @Valid @RequestBody LibroRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.actualizarLibro(id, req)));
    }

    @DeleteMapping("/libros/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar libro (soft delete)")
    public ResponseEntity<ApiResponse<Void>> eliminarLibro(@PathVariable Long id) {
        catalogoService.eliminarLibro(id);
        return ResponseEntity.ok(ApiResponse.ok("Libro eliminado", null));
    }

    // ===== EJEMPLARES =====

    @GetMapping("/libros/{libroId}/ejemplares")
    @Operation(summary = "Listar ejemplares físicos de un libro")
    public ResponseEntity<ApiResponse<List<EjemplarResponse>>> listarEjemplares(@PathVariable Long libroId) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.listarEjemplares(libroId)));
    }

    @PostMapping("/ejemplares")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Agregar ejemplar físico")
    public ResponseEntity<ApiResponse<EjemplarResponse>> crearEjemplar(@Valid @RequestBody EjemplarRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(catalogoService.crearEjemplar(req)));
    }

    @PatchMapping("/ejemplares/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Cambiar estado de ejemplar (DISPONIBLE, EN_MANTENIMIENTO, BAJA, etc.)")
    public ResponseEntity<ApiResponse<EjemplarResponse>> actualizarEstadoEjemplar(
            @PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.actualizarEstadoEjemplar(id, estado)));
    }

    @DeleteMapping("/ejemplares/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar ejemplar")
    public ResponseEntity<ApiResponse<Void>> eliminarEjemplar(@PathVariable Long id) {
        catalogoService.eliminarEjemplar(id);
        return ResponseEntity.ok(ApiResponse.ok("Ejemplar eliminado", null));
    }

    // ===== LIBROS DIGITALES =====

    @GetMapping("/libros/{libroId}/digitales")
    @Operation(summary = "Listar versiones digitales de un libro")
    public ResponseEntity<ApiResponse<List<LibroDigitalResponse>>> listarDigitales(@PathVariable Long libroId) {
        return ResponseEntity.ok(ApiResponse.ok(catalogoService.listarLibrosDigitales(libroId)));
    }

    @PostMapping("/digitales")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Agregar versión digital de un libro")
    public ResponseEntity<ApiResponse<LibroDigitalResponse>> crearDigital(@Valid @RequestBody LibroDigitalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(catalogoService.crearLibroDigital(req)));
    }

    @DeleteMapping("/digitales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar libro digital")
    public ResponseEntity<ApiResponse<Void>> eliminarDigital(@PathVariable Long id) {
        catalogoService.eliminarLibroDigital(id);
        return ResponseEntity.ok(ApiResponse.ok("Libro digital eliminado", null));
    }
}
