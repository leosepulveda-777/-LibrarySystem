package com.library.repository;

import com.library.entity.Ejemplar;
import com.library.enums.EstadoEjemplar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {
    List<Ejemplar> findByLibroIdAndActivoTrue(Long libroId);
    List<Ejemplar> findByLibroIdAndEstadoAndActivoTrue(Long libroId, EstadoEjemplar estado);
    boolean existsByCodigoInventario(String codigoInventario);
    Optional<Ejemplar> findByCodigoInventario(String codigoInventario);
    long countByLibroIdAndEstadoAndActivoTrue(Long libroId, EstadoEjemplar estado);

    default Optional<Ejemplar> findFirstDisponibleByLibroId(Long libroId) {
        return findByLibroIdAndEstadoAndActivoTrue(libroId, EstadoEjemplar.DISPONIBLE)
                .stream().findFirst();
    }
}
