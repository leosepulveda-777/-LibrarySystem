package com.library.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class ReporteInventarioResponse {
    private long totalLibros;
    private long totalEjemplares;
    private long ejemplaresDisponibles;
    private long ejemplaresPrestados;
    private long ejemplaresEnMantenimiento;
    private long totalLibrosDigitales;
    private List<LibroResponse> librosMasPrestados;
    private List<CategoriaResponse> categoriasMasPopulares;
}
