package com.library.entity;

import com.library.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"lector","libro"})
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lector_id", nullable = false)
    private Lector lector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "fecha_reserva", nullable = false)
    @Builder.Default
    private LocalDateTime fechaReserva = LocalDateTime.now();

    @Column(name = "fecha_disponible")
    private LocalDateTime fechaDisponible;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(name = "posicion_cola")
    private Integer posicionCola;

    @Column(name = "observaciones", length = 500)
    private String observaciones;
}
