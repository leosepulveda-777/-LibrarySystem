package com.library.config;

import com.library.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de Spring Security
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    private static final String[] PUBLIC_URLS = {
            "/api/v1/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                // Notificaciones y recomendaciones (microservicios simulados)
                .requestMatchers("/api/v1/notifications/**").hasAnyRole("ADMIN", "BIBLIOTECARIO")
                .requestMatchers("/api/v1/recommendations/**").authenticated()
                // Catálogo - lectura pública, escritura restringida
                .requestMatchers(HttpMethod.GET, "/api/v1/catalogo/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/catalogo/**").hasAnyRole("ADMIN", "BIBLIOTECARIO")
                .requestMatchers(HttpMethod.PUT, "/api/v1/catalogo/**").hasAnyRole("ADMIN", "BIBLIOTECARIO")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/catalogo/**").hasRole("ADMIN")
                // Préstamos
                .requestMatchers("/api/v1/prestamos/**").hasAnyRole("ADMIN", "BIBLIOTECARIO", "LECTOR")
                // Reservas
                .requestMatchers("/api/v1/reservas/**").authenticated()
                // Multas
                .requestMatchers(HttpMethod.DELETE, "/api/v1/multas/*/condonar").hasRole("ADMIN")
                .requestMatchers("/api/v1/multas/**").hasAnyRole("ADMIN", "BIBLIOTECARIO", "LECTOR")
                // Lectores
                .requestMatchers("/api/v1/lectores/**").hasAnyRole("ADMIN", "BIBLIOTECARIO", "LECTOR")
                // Reportes
                .requestMatchers("/api/v1/reportes/**").hasAnyRole("ADMIN", "BIBLIOTECARIO")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
