package com.library.controller;

import com.library.dto.request.PrestamoRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.PrestamoResponse;
import com.library.service.impl.PrestamoService;
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

@RestController
@RequestMapping("/api/v1/prestamos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Préstamos", description = "Gestión de préstamos físicos y digitales")
public class PrestamoController {

    private final PrestamoService prestamoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Listar todos los préstamos (paginado)")
    public ResponseEntity<ApiResponse<Page<PrestamoResponse>>> listarTodos(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(prestamoService.listarTodos(estado, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener préstamo por ID")
    public ResponseEntity<ApiResponse<PrestamoResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(prestamoService.obtenerPrestamo(id)));
    }

    @GetMapping("/lector/{lectorId}")
    @Operation(summary = "Listar préstamos de un lector")
    public ResponseEntity<ApiResponse<Page<PrestamoResponse>>> porLector(
            @PathVariable Long lectorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(prestamoService.listarPrestamosPorLector(lectorId, page, size)));
    }

    @PostMapping("/fisico")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Registrar préstamo físico", description = "El bibliotecario asigna un ejemplar a un lector")
    public ResponseEntity<ApiResponse<PrestamoResponse>> prestarFisico(@Valid @RequestBody PrestamoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(prestamoService.prestarFisico(req)));
    }

    @PostMapping("/digital")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO','LECTOR')")
    @Operation(summary = "Registrar préstamo digital", description = "El lector toma prestado un libro digital")
    public ResponseEntity<ApiResponse<PrestamoResponse>> prestarDigital(@Valid @RequestBody PrestamoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(prestamoService.prestarDigital(req)));
    }

    @PatchMapping("/{id}/devolver")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Registrar devolución", description = "Devuelve el préstamo y genera multa si hay retraso")
    public ResponseEntity<ApiResponse<PrestamoResponse>> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Devolución registrada", prestamoService.devolver(id)));
    }

    @PatchMapping("/{id}/renovar")
    @Operation(summary = "Renovar préstamo", description = "Extiende el plazo del préstamo activo")
    public ResponseEntity<ApiResponse<PrestamoResponse>> renovar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Préstamo renovado", prestamoService.renovar(id)));
    }
}
