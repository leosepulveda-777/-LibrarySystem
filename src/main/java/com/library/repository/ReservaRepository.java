package com.library.repository;

import com.library.entity.Reserva;
import com.library.enums.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    Page<Reserva> findByLectorId(Long lectorId, Pageable pageable);
    long countByLectorIdAndEstadoIn(Long lectorId, List<EstadoReserva> estados);
    boolean existsByLectorIdAndLibroIdAndEstadoIn(Long lectorId, Long libroId, List<EstadoReserva> estados);

    @Query("SELECT r FROM Reserva r WHERE r.libro.id = :libroId AND r.estado = 'PENDIENTE' " +
           "ORDER BY r.posicionCola ASC")
    List<Reserva> findColaByLibroId(@Param("libroId") Long libroId);

    @Query("SELECT MAX(r.posicionCola) FROM Reserva r WHERE r.libro.id = :libroId AND r.estado = 'PENDIENTE'")
    Optional<Integer> findMaxPosicionCola(@Param("libroId") Long libroId);

    @Query("SELECT r FROM Reserva r WHERE r.estado = 'DISPONIBLE' AND r.fechaExpiracion < :ahora")
    List<Reserva> findExpiradas(@Param("ahora") LocalDateTime ahora);

    Optional<Reserva> findFirstByLibroIdAndEstadoOrderByPosicionColaAsc(Long libroId, EstadoReserva estado);
}
