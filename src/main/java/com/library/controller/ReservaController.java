package com.library.controller;

import com.library.dto.request.ReservaRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.ReservaResponse;
import com.library.service.impl.ReservaService;
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
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reservas", description = "Gestión de reservas y cola de espera")
public class ReservaController {

    private final ReservaService reservaService;

    // ✅ NUEVO: listar todas las reservas (admin/bibliotecario)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Listar todas las reservas paginadas")
    public ResponseEntity<ApiResponse<Page<ReservaResponse>>> listarTodas(
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.ok(reservaService.listarTodas(estado, page, size)));
    }

    @PostMapping("/lector/{lectorId}")
    @Operation(summary = "Crear reserva para un libro")
    public ResponseEntity<ApiResponse<ReservaResponse>> reservar(
            @PathVariable Long lectorId,
            @Valid @RequestBody ReservaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(reservaService.reservar(lectorId, req)));
    }

    @PatchMapping("/{reservaId}/cancelar")
    @Operation(summary = "Cancelar una reserva")
    public ResponseEntity<ApiResponse<ReservaResponse>> cancelar(
            @PathVariable Long reservaId,
            @RequestParam Long lectorId) {
        return ResponseEntity.ok(ApiResponse.ok("Reserva cancelada",
                reservaService.cancelar(reservaId, lectorId)));
    }

    @PatchMapping("/{reservaId}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
    @Operation(summary = "Confirmar entrega de libro al lector (admin/bibliotecario)")
    public ResponseEntity<ApiResponse<ReservaResponse>> confirmar(
            @PathVariable Long reservaId) {
        return ResponseEntity.ok(ApiResponse.ok("Entrega confirmada",
                reservaService.confirmarEntrega(reservaId)));
    }

    @GetMapping("/lector/{lectorId}")
    @Operation(summary = "Listar reservas de un lector")
    public ResponseEntity<ApiResponse<Page<ReservaResponse>>> porLector(
            @PathVariable Long lectorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(reservaService.listarPorLector(lectorId, page, size)));
    }

    @GetMapping("/libro/{libroId}/cola")
    @Operation(summary = "Ver cola de espera para un libro")
    public ResponseEntity<ApiResponse<List<ReservaResponse>>> cola(@PathVariable Long libroId) {
        return ResponseEntity.ok(ApiResponse.ok(reservaService.verCola(libroId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reserva por ID")
    public ResponseEntity<ApiResponse<ReservaResponse>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reservaService.obtenerReserva(id)));
    }
}