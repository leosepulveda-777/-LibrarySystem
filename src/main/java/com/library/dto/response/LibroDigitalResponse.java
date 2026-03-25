package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class LibroDigitalResponse {
    private Long id;
    private Long libroId;
    private String tituloLibro;
    private String formato;
    private String urlArchivo;
    private Double tamanioMb;
    private int maxPrestamosSimultaneos;
    private int prestamosActivos;
    private boolean disponible;
    private boolean activo;
    private LocalDateTime createdAt;
}
