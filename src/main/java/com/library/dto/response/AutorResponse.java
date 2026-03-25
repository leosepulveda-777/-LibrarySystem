package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data @Builder
public class AutorResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String biografia;
    private String nacionalidad;
    private LocalDate fechaNacimiento;
    private LocalDate fechaFallecimiento;
    private boolean activo;
}
