package com.library.repository;

import com.library.entity.Multa;
import com.library.enums.EstadoMulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MultaRepository extends JpaRepository<Multa, Long> {
    Page<Multa> findByLectorId(Long lectorId, Pageable pageable);
    List<Multa> findByLectorIdAndEstadoIn(Long lectorId, List<EstadoMulta> estados);
    boolean existsByPrestamoId(Long prestamoId);
    long countByEstado(EstadoMulta estado);

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Multa m WHERE m.estado IN ('PENDIENTE','PARCIALMENTE_PAGADA')")
    BigDecimal sumMontoPendiente();

    @Query("SELECT COALESCE(SUM(m.montoPagado), 0) FROM Multa m")
    BigDecimal sumMontoCobrado();

    @Query("SELECT COALESCE(SUM(m.monto), 0) FROM Multa m")
    BigDecimal sumMontoTotal();

    @Query("SELECT m FROM Multa m WHERE m.fechaGeneracion BETWEEN :desde AND :hasta")
    Page<Multa> findByFechaBetween(@Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta,
                                   Pageable pageable);
}
