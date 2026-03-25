package com.library.dto.response;

import com.library.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private Role rol;
    private boolean activo;
    private LocalDateTime createdAt;
}
