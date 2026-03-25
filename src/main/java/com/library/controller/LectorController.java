package com.library.controller;

import com.library.dto.request.LectorUpdateRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.LectorResponse;
import com.library.service.impl.LectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lectores")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lectores", description = "Gestión de lectores y perfil de usuario")
public class LectorController {

    private final LectorService lectorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Listar lectores con búsqueda y paginación")
    public ResponseEntity<ApiResponse<Page<LectorResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(lectorService.listar(search, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lector por ID")
    public ResponseEntity<ApiResponse<LectorResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(lectorService.obtener(id)));
    }

    @GetMapping("/carnet/{numeroCarnet}")
    @Operation(summary = "Obtener lector por número de carnet")
    public ResponseEntity<ApiResponse<LectorResponse>> porCarnet(@PathVariable String numeroCarnet) {
        return ResponseEntity.ok(ApiResponse.ok(lectorService.obtenerPorCarnet(numeroCarnet)));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener perfil de lector por ID de usuario")
    public ResponseEntity<ApiResponse<LectorResponse>> porUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(ApiResponse.ok(lectorService.obtenerPorUsuarioId(usuarioId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos del lector")
    public ResponseEntity<ApiResponse<LectorResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LectorUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Lector actualizado", lectorService.actualizar(id, req)));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Activar lector")
    public ResponseEntity<ApiResponse<LectorResponse>> activar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lector activado", lectorService.toggleActivo(id, true)));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Desactivar lector")
    public ResponseEntity<ApiResponse<LectorResponse>> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Lector desactivado", lectorService.toggleActivo(id, false)));
    }
}
