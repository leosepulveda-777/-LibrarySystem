package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "El refresh token es obligatorio")
    private String refreshToken;
}
