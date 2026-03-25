package com.library.entity;

import com.library.enums.EstadoEjemplar;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ejemplares")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"libro","prestamos"})
public class Ejemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    @Column(name = "codigo_inventario", nullable = false, unique = true)
    private String codigoInventario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoEjemplar estado = EstadoEjemplar.DISPONIBLE;

    @Column(name = "ubicacion_fisica")
    private String ubicacionFisica;

    @Column(name = "condicion")
    private String condicion;

    @Column(name = "fecha_adquisicion")
    private LocalDateTime fechaAdquisicion;

    @Column(name = "activo")
    @Builder.Default
    private boolean activo = true;

    @OneToMany(mappedBy = "ejemplar", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prestamo> prestamos = new ArrayList<>();
}
