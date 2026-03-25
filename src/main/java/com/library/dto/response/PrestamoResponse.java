package com.library.dto.response;

import com.library.enums.EstadoPrestamo;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class PrestamoResponse {
    private Long id;
    private Long lectorId;
    private String nombreLector;
    private String numeroCarnet;
    private Long libroId;
    private String tituloLibro;
    private String isbnLibro;
    private Long ejemplarId;
    private String codigoEjemplar;
    private Long libroDigitalId;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaDevolucionEsperada;
    private LocalDateTime fechaDevolucionReal;
    private EstadoPrestamo estado;
    private int numeroRenovaciones;
    private boolean esDigital;
    private String observaciones;
    private boolean vencido;
    private long diasRetraso;
}
