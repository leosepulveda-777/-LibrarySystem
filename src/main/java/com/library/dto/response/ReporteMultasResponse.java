package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ReporteMultasResponse {
    private LocalDateTime desde;
    private LocalDateTime hasta;
    private long totalMultas;
    private long multasPendientes;
    private long multasPagadas;
    private long multasCondonadas;
    private BigDecimal montoTotal;
    private BigDecimal montoCobrado;
    private BigDecimal montoPendiente;
    private List<MultaResponse> detalle;
}
