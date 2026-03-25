package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LibroDigitalRequest {

    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @NotBlank(message = "El formato es obligatorio")
    private String formato; // PDF, EPUB, MOBI

    private String urlArchivo;

    private Double tamanioMb;

    private Integer maxPrestamosSimultaneos = 5;
}
