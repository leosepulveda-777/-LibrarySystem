package com.library.repository;

import com.library.entity.Prestamo;
import com.library.enums.EstadoPrestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    Page<Prestamo> findByLectorId(Long lectorId, Pageable pageable);
    Page<Prestamo> findByEstado(EstadoPrestamo estado, Pageable pageable);
    long countByLectorIdAndEstado(Long lectorId, EstadoPrestamo estado);
    boolean existsByLectorIdAndLibroIdAndEstado(Long lectorId, Long libroId, EstadoPrestamo estado);

    @Query("SELECT p FROM Prestamo p WHERE p.estado = 'ACTIVO' AND p.fechaDevolucionEsperada < :ahora")
    List<Prestamo> findVencidos(@Param("ahora") LocalDateTime ahora);

    @Query("SELECT p FROM Prestamo p WHERE p.estado = 'ACTIVO' AND " +
           "p.fechaDevolucionEsperada BETWEEN :desde AND :hasta")
    List<Prestamo> findPorVencer(@Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta);

    @Query("SELECT p FROM Prestamo p WHERE p.fechaPrestamo BETWEEN :desde AND :hasta")
    Page<Prestamo> findByFechaPrestamoBetween(@Param("desde") LocalDateTime desde,
                                               @Param("hasta") LocalDateTime hasta,
                                               Pageable pageable);

    long countByEstado(EstadoPrestamo estado);
    long countByEsDigital(boolean esDigital);
}
