package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AutorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String apellido;

    @Size(max = 1000)
    private String biografia;

    private String nacionalidad;

    private String fechaNacimiento; // yyyy-MM-dd

    private String fechaFallecimiento; // yyyy-MM-dd
}
