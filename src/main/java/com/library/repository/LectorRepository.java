package com.library.repository;

import com.library.entity.Lector;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LectorRepository extends JpaRepository<Lector, Long> {
    Optional<Lector> findByNumeroCarnet(String numeroCarnet);
    Optional<Lector> findByUsuarioId(Long usuarioId);
    boolean existsByNumeroCarnet(String numeroCarnet);

    @Query("SELECT l FROM Lector l WHERE l.activo = true AND " +
            "(:search IS NULL OR LOWER(l.usuario.nombre) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(l.usuario.apellido) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(l.usuario.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(l.numeroCarnet) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Lector> findBySearchTerm(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.lector.id = :lectorId AND p.estado = 'ACTIVO'")
    long countPrestamosActivos(@Param("lectorId") Long lectorId);
}