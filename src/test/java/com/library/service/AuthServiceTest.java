package com.library.service;

import com.library.config.LibraryProperties;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.AuthResponse;
import com.library.entity.Lector;
import com.library.entity.RefreshToken;
import com.library.entity.Usuario;
import com.library.enums.Role;
import com.library.exception.DuplicateResourceException;
import com.library.repository.LectorRepository;
import com.library.repository.RefreshTokenRepository;
import com.library.repository.UsuarioRepository;
import com.library.security.jwt.JwtService;
import com.library.service.impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitarios")
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LectorRepository lectorRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Usuario usuarioMock;
    private Lector lectorMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604800000L);
        ReflectionTestUtils.setField(authService, "jwtExpiration", 900000L);

        registerRequest = new RegisterRequest();
        registerRequest.setNombre("Juan");
        registerRequest.setApellido("Pérez");
        registerRequest.setEmail("juan@test.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setTelefono("3001234567");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("juan@test.com");
        loginRequest.setPassword("Password123!");

        usuarioMock = Usuario.builder()
                .id(1L).nombre("Juan").apellido("Pérez")
                .email("juan@test.com").password("encodedPass")
                .telefono("3001234567").rol(Role.LECTOR).activo(true)
                .build();

        lectorMock = Lector.builder()
                .id(1L).usuario(usuarioMock).numeroCarnet("LIB-000001").activo(true)
                .build();

        usuarioMock.setLector(lectorMock);
    }

    @Test
    @DisplayName("register() - debe crear usuario y lector exitosamente")
    void register_ShouldCreateUserAndLector() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(usuarioRepository.save(any())).thenReturn(usuarioMock);
        when(lectorRepository.existsByNumeroCarnet(anyString())).thenReturn(false);
        when(lectorRepository.save(any())).thenReturn(lectorMock);
        when(refreshTokenRepository.findAll()).thenReturn(new ArrayList<>());
        when(refreshTokenRepository.save(any())).thenReturn(
                RefreshToken.builder().token("refresh-token").expiryDate(Instant.now().plusSeconds(3600)).build());
        when(jwtService.generateToken(any())).thenReturn("access-token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRol()).isEqualTo(Role.LECTOR);
        verify(usuarioRepository).save(any(Usuario.class));
        verify(lectorRepository).save(any(Lector.class));
    }

    @Test
    @DisplayName("register() - debe lanzar excepción si email ya existe")
    void register_ShouldThrowIfEmailExists() {
        when(usuarioRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("juan@test.com");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("login() - debe autenticar y retornar tokens")
    void login_ShouldAuthenticateAndReturnTokens() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuarioMock));
        when(jwtService.generateToken(any())).thenReturn("access-token");
        when(refreshTokenRepository.findAll()).thenReturn(new ArrayList<>());
        when(refreshTokenRepository.save(any())).thenReturn(
                RefreshToken.builder().token("refresh-token").expiryDate(Instant.now().plusSeconds(3600)).build());

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("juan@test.com");
        assertThat(response.getNumeroCarnet()).isEqualTo("LIB-000001");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("login() - debe lanzar excepción si usuario está inactivo")
    void login_ShouldThrowIfUserInactive() {
        usuarioMock.setActivo(false);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(usuarioMock));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(com.library.exception.BusinessException.class)
                .hasMessageContaining("desactivada");
    }
}
