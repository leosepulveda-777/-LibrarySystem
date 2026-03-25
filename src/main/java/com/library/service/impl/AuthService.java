package com.library.service.impl;

import com.library.dto.request.LoginRequest;
import com.library.dto.request.RefreshTokenRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.AuthResponse;
import com.library.entity.Lector;
import com.library.entity.RefreshToken;
import com.library.entity.Usuario;
import com.library.enums.Role;
import com.library.exception.BusinessException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.TokenExpiredException;
import com.library.repository.LectorRepository;
import com.library.repository.RefreshTokenRepository;
import com.library.repository.UsuarioRepository;
import com.library.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final LectorRepository lectorRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${application.jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email: " + request.getEmail());
        }
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre()).apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .rol(Role.LECTOR).activo(true).build();
        usuario = usuarioRepository.save(usuario);

        String numeroCarnet = generarNumeroCarnet();
        LocalDate fechaNac = parseFecha(request.getFechaNacimiento());

        Lector lector = Lector.builder()
                .usuario(usuario).numeroCarnet(numeroCarnet)
                .fechaNacimiento(fechaNac).direccion(request.getDireccion())
                .activo(true).build();
        lector = lectorRepository.save(lector);
        usuario.setLector(lector);

        log.info("Nuevo lector registrado: {} carnet={}", usuario.getEmail(), numeroCarnet);
        String accessToken = jwtService.generateToken(usuario);
        String refreshToken = createRefreshToken(usuario);
        return buildResponse(usuario, lector, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!usuario.isActivo()) {
            throw new BusinessException("La cuenta está desactivada. Contacte al administrador.");
        }
        String accessToken = jwtService.generateToken(usuario);
        String refreshToken = createRefreshToken(usuario);
        log.info("Login exitoso: {}", usuario.getEmail());
        return buildResponse(usuario, usuario.getLector(), accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenExpiredException("Refresh token inválido"));
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw new TokenExpiredException("Refresh token expirado. Inicie sesión nuevamente.");
        }
        Usuario usuario = rt.getUsuario();
        String newAccessToken = jwtService.generateToken(usuario);
        return buildResponse(usuario, usuario.getLector(), newAccessToken, rt.getToken());
    }

    private String createRefreshToken(Usuario usuario) {
        // Delete existing token for this user if any
        refreshTokenRepository.deleteByUsuario(usuario);
        RefreshToken rt = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .build();
        return refreshTokenRepository.save(rt).getToken();
    }

    private String generarNumeroCarnet() {
        String carnet;
        do {
            carnet = "LIB-" + String.format("%06d", (int)(Math.random() * 999999));
        } while (lectorRepository.existsByNumeroCarnet(carnet));
        return carnet;
    }

    private LocalDate parseFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) return null;
        try { return LocalDate.parse(fecha); }
        catch (DateTimeParseException e) { return null; }
    }

    private AuthResponse buildResponse(Usuario u, Lector l, String access, String refresh) {
        return AuthResponse.builder()
                .accessToken(access).refreshToken(refresh)
                .tokenType("Bearer").expiresIn(jwtExpiration / 1000)
                .userId(u.getId()).email(u.getEmail())
                .nombre(u.getNombre()).apellido(u.getApellido())
                .rol(u.getRol())
                .numeroCarnet(l != null ? l.getNumeroCarnet() : null)
                .build();
    }
}
