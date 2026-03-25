package com.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaRequest {

    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    private String observaciones;
}
