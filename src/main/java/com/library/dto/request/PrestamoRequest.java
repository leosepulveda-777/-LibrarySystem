package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrestamoRequest {

    @NotNull(message = "El ID del lector es obligatorio")
    private Long lectorId;

    private Long ejemplarId;      // para préstamo físico

    private Long libroDigitalId;  // para préstamo digital

    private Long libroId;         // opcional

    private String observaciones;
}