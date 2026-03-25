package com.library.dto.response;

import com.library.enums.EstadoEjemplar;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class EjemplarResponse {
    private Long id;
    private Long libroId;
    private String tituloLibro;
    private String codigoInventario;
    private EstadoEjemplar estado;
    private String ubicacionFisica;
    private String condicion;
    private LocalDateTime fechaAdquisicion;
    private boolean activo;
}
