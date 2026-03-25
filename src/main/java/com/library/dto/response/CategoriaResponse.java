package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long padreId;
    private String nombrePadre;
    private boolean activa;
    private List<CategoriaResponse> subcategorias;
}
