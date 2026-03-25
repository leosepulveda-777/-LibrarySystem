package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "libros_digitales")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "libro")
public class LibroDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(nullable = false)
    private String formato;

    @Column(name = "url_archivo")
    private String urlArchivo;

    @Column(name = "tamanio_mb")
    private Double tamanioMb;

    @Column(name = "max_prestamos_simultaneos")
    @Builder.Default
    private int maxPrestamosSimultaneos = 5;

    @Column(name = "prestamos_activos")
    @Builder.Default
    private int prestamosActivos = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
