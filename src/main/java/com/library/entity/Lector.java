package com.library.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lectores", uniqueConstraints = {
        @UniqueConstraint(columnNames = "numero_carnet")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario","prestamos","reservas","multas"})
public class Lector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "numero_carnet", nullable = false, unique = true)
    private String numeroCarnet;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 500)
    private String direccion;

    @Column(name = "fecha_registro", updatable = false)
    @Builder.Default
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "activo")
    @Builder.Default
    private boolean activo = true;

    @Column(name = "max_prestamos")
    @Builder.Default
    private int maxPrestamos = 3;

    @OneToMany(mappedBy = "lector", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prestamo> prestamos = new ArrayList<>();

    @OneToMany(mappedBy = "lector", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reserva> reservas = new ArrayList<>();

    @OneToMany(mappedBy = "lector", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Multa> multas = new ArrayList<>();
}
