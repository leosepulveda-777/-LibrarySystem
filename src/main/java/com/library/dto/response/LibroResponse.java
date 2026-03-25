package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class LibroResponse {
    private Long id;
    private String titulo;
    private String isbn;
    private String sinopsis;
    private Integer anioPublicacion;
    private String editorial;
    private String idioma;
    private Integer numeroPaginas;
    private String imagenPortada;
    private boolean activo;
    private LocalDateTime createdAt;
    private List<AutorResponse> autores;
    private List<CategoriaResponse> categorias;
    private int totalEjemplares;
    private int ejemplaresDisponibles;
    private boolean disponibleDigital;
}
