package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data @Builder
public class ReportePrestamosResponse {
    private LocalDateTime desde;
    private LocalDateTime hasta;
    private long totalPrestamos;
    private long prestamosActivos;
    private long prestamosVencidos;
    private long prestamosDevueltos;
    private long prestamosDigitales;
    private long prestamosFisicos;
    private List<PrestamoResponse> detalle;
}
