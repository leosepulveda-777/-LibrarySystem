package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.enums.TipoNotificacion;
import com.library.service.impl.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notificaciones", description = "Microservicio simulado de notificaciones (se registran en logs)")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Enviar notificación manual",
               description = "Registra la notificación en el sistema de logs (simulado)")
    public ResponseEntity<ApiResponse<String>> enviar(@RequestBody NotificacionRequest req) {
        notificationService.enviar(req.getUsuarioId(), req.getEmail(),
                req.getTipo(), req.getMensaje());
        return ResponseEntity.ok(ApiResponse.ok("Notificación enviada y registrada en logs", req.getTipo().name()));
    }

    @Data
    static class NotificacionRequest {
        @NotNull private Long usuarioId;
        @NotBlank private String email;
        @NotNull private TipoNotificacion tipo;
        @NotBlank private String mensaje;
    }
}
