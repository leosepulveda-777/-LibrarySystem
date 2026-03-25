package com.library.controller;

import com.library.dto.request.LoginRequest;
import com.library.dto.request.RefreshTokenRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.AuthResponse;
import com.library.service.impl.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login y refresh de tokens JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo lector", description = "Crea una cuenta de lector y retorna tokens JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica con email y contraseña, retorna JWT")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Login exitoso", authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar access token", description = "Usa el refresh token para obtener un nuevo access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refreshToken(request)));
    }
}
