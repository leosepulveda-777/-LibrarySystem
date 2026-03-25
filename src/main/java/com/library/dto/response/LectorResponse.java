package com.library.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LectorResponse {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String numeroCarnet;
    private LocalDate fechaNacimiento;
    private String direccion;
    private LocalDateTime fechaRegistro;
    private boolean activo;
    private int maxPrestamos;
    private int prestamosActivos;
    private int multasPendientes;
}
