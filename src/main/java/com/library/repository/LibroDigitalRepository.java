package com.library.repository;

import com.library.entity.LibroDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibroDigitalRepository extends JpaRepository<LibroDigital, Long> {
    List<LibroDigital> findByLibroIdAndActivoTrue(Long libroId);

    @Query("SELECT ld FROM LibroDigital ld WHERE ld.libro.id = :libroId AND ld.activo = true " +
           "AND ld.prestamosActivos < ld.maxPrestamosSimultaneos")
    Optional<LibroDigital> findDisponibleByLibroId(@Param("libroId") Long libroId);
}
