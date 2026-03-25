package com.library.controller;

import com.library.dto.request.CondonacionRequest;
import com.library.dto.request.PagoMultaRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.MultaResponse;
import com.library.service.impl.MultaService;
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
@RequestMapping("/api/v1/multas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Multas", description = "Gestión de multas: pago, condonación y consulta")
public class MultaController {

    private final MultaService multaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Listar todas las multas")
    public ResponseEntity<ApiResponse<Page<MultaResponse>>> listarTodas(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(multaService.listarTodas(estado, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener multa por ID")
    public ResponseEntity<ApiResponse<MultaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(multaService.obtener(id)));
    }

    @GetMapping("/lector/{lectorId}")
    @Operation(summary = "Listar multas de un lector")
    public ResponseEntity<ApiResponse<Page<MultaResponse>>> porLector(
            @PathVariable Long lectorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(multaService.listarPorLector(lectorId, page, size)));
    }

    @PostMapping("/{id}/pagar")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Registrar pago (parcial o total) de multa")
    public ResponseEntity<ApiResponse<MultaResponse>> pagar(
            @PathVariable Long id,
            @Valid @RequestBody PagoMultaRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Pago registrado", multaService.pagar(id, req)));
    }

    @PostMapping("/{id}/condonar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Condonar multa (solo ADMIN)")
    public ResponseEntity<ApiResponse<MultaResponse>> condonar(
            @PathVariable Long id,
            @Valid @RequestBody CondonacionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Multa condonada", multaService.condonar(id, req)));
    }
}
