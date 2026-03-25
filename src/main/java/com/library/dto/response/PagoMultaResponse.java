package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class PagoMultaResponse {
    private Long id;
    private Long multaId;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private String metodoPago;
    private String referenciaPago;
    private String registradoPor;
    private String observaciones;
}
