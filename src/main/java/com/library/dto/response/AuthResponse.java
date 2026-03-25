package com.library.dto.response;

import com.library.enums.Role;
import lombok.Builder;
import lombok.Data;

/**
 * Respuesta de autenticación con tokens JWT
 */
@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String email;
    private String nombre;
    private String apellido;
    private Role rol;
    private String numeroCarnet; // solo para LECTOR
}
