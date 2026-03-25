package com.library.controller;

import com.library.dto.response.*;
import com.library.service.impl.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Reportes de préstamos, multas e inventario")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/prestamos")
    @Operation(summary = "Reporte de préstamos por período")
    public ResponseEntity<ApiResponse<ReportePrestamosResponse>> reportePrestamos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.reportePrestamos(desde, hasta)));
    }

    @GetMapping("/multas")
    @Operation(summary = "Reporte de multas por período")
    public ResponseEntity<ApiResponse<ReporteMultasResponse>> reporteMultas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.reporteMultas(desde, hasta)));
    }

    @GetMapping("/inventario")
    @Operation(summary = "Reporte de inventario actual")
    public ResponseEntity<ApiResponse<ReporteInventarioResponse>> reporteInventario() {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.reporteInventario()));
    }
}
