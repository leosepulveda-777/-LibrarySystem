package com.library.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LectorUpdateRequest {

    @Size(min = 2, max = 100)
    private String nombre;

    @Size(min = 2, max = 100)
    private String apellido;

    @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$")
    private String telefono;

    @Size(max = 500)
    private String direccion;

    private String fechaNacimiento;

    private Boolean activo;
}
