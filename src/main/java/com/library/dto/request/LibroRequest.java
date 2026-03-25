package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class LibroRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 500)
    private String titulo;

    @NotBlank(message = "El ISBN es obligatorio")
    @Size(max = 20)
    private String isbn;

    @Size(max = 2000)
    private String sinopsis;

    private Integer anioPublicacion;

    private String editorial;

    private String idioma;

    private Integer numeroPaginas;

    private String imagenPortada;

    @NotEmpty(message = "Debe especificar al menos un autor")
    private List<Long> autorIds;

    @NotEmpty(message = "Debe especificar al menos una categoría")
    private List<Long> categoriaIds;
}
