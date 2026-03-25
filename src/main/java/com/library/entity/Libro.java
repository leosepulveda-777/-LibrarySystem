package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "libros", uniqueConstraints = {
        @UniqueConstraint(columnNames = "isbn")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"autores","categorias","ejemplares","librosDigitales","reservas"})
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(length = 2000)
    private String sinopsis;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    private String editorial;

    private String idioma;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(name = "imagen_portada")
    private String imagenPortada;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "libro_autor",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id")
    )
    @Builder.Default
    private List<Autor> autores = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "libro_categoria",
            joinColumns = @JoinColumn(name = "libro_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private List<Categoria> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Ejemplar> ejemplares = new ArrayList<>();

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LibroDigital> librosDigitales = new ArrayList<>();

    @OneToMany(mappedBy = "libro", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reserva> reservas = new ArrayList<>();
}
