package com.library.dto.response;

import com.library.enums.EstadoMulta;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class MultaResponse {
    private Long id;
    private Long lectorId;
    private String nombreLector;
    private String numeroCarnet;
    private Long prestamoId;
    private String tituloLibro;
    private BigDecimal monto;
    private BigDecimal montoPagado;
    private BigDecimal montoPendiente;
    private Integer diasRetraso;
    private EstadoMulta estado;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaPago;
    private String motivoCondonacion;
    private List<PagoMultaResponse> pagos;
}
