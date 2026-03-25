package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EjemplarRequest {

    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @NotBlank(message = "El código de inventario es obligatorio")
    private String codigoInventario;

    private String ubicacionFisica;

    private String condicion; // BUENO, REGULAR, MALO
}
