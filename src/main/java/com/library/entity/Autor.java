package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autores")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "libros")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(length = 1000)
    private String biografia;

    private String nacionalidad;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_fallecimiento")
    private LocalDate fechaFallecimiento;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @ManyToMany(mappedBy = "autores")
    @Builder.Default
    private List<Libro> libros = new ArrayList<>();
}
