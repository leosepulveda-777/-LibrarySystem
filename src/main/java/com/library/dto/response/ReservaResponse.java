package com.library.dto.response;

import com.library.enums.EstadoReserva;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ReservaResponse {
    private Long id;
    private Long lectorId;
    private String nombreLector;
    private String numeroCarnet;
    private Long libroId;
    private String tituloLibro;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaDisponible;
    private LocalDateTime fechaExpiracion;
    private EstadoReserva estado;
    private Integer posicionCola;
    private String observaciones;
}
