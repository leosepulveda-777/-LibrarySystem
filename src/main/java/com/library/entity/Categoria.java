package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"padre","subcategorias","libros"})
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private Categoria padre;

    @OneToMany(mappedBy = "padre", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Categoria> subcategorias = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean activa = true;

    @ManyToMany(mappedBy = "categorias")
    @Builder.Default
    private List<Libro> libros = new ArrayList<>();
}
