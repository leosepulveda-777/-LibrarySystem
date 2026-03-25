package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.dto.response.LibroResponse;
import com.library.service.impl.RecomendacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recomendaciones", description = "Microservicio de recomendaciones basado en historial del lector")
public class RecomendacionController {

    private final RecomendacionService recomendacionService;

    @GetMapping("/{lectorId}")
    @Operation(summary = "Obtener recomendaciones para un lector",
               description = "Algoritmo basado en categorías de libros prestados previamente. Máximo 10 resultados.")
    public ResponseEntity<ApiResponse<List<LibroResponse>>> recomendar(@PathVariable Long lectorId) {
        return ResponseEntity.ok(ApiResponse.ok(recomendacionService.recomendar(lectorId)));
    }
}
