package com.library.entity;

import com.library.enums.EstadoMulta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "multas")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"lector","prestamo","condonadaPor","pagos"})
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lector_id", nullable = false)
    private Lector lector;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestamo_id", nullable = false, unique = true)
    private Prestamo prestamo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "dias_retraso", nullable = false)
    private Integer diasRetraso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoMulta estado = EstadoMulta.PENDIENTE;

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "motivo_condonacion", length = 500)
    private String motivoCondonacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condonada_por")
    private Usuario condonadaPor;

    @OneToMany(mappedBy = "multa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PagoMulta> pagos = new ArrayList<>();
}
