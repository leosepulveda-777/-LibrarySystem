package com.library.entity;

import com.library.enums.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"lector","ejemplar","libroDigital","libro","bibliotecario","multa"})
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lector_id", nullable = false)
    private Lector lector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ejemplar_id")
    private Ejemplar ejemplar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_digital_id")
    private LibroDigital libroDigital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "fecha_prestamo", nullable = false)
    @Builder.Default
    private LocalDateTime fechaPrestamo = LocalDateTime.now();

    @Column(name = "fecha_devolucion_esperada", nullable = false)
    private LocalDateTime fechaDevolucionEsperada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;

    @Column(name = "numero_renovaciones")
    @Builder.Default
    private int numeroRenovaciones = 0;

    @Column(name = "es_digital")
    @Builder.Default
    private boolean esDigital = false;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bibliotecario_id")
    private Usuario bibliotecario;

    @OneToOne(mappedBy = "prestamo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Multa multa;
}
