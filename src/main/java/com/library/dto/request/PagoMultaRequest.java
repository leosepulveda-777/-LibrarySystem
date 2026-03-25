package com.library.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoMultaRequest {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA

    private String referenciaPago;

    private String observaciones;
}
