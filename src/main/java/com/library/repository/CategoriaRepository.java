package com.library.repository;

import com.library.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombre(String nombre);
    List<Categoria> findByPadreIsNullAndActivaTrue();
    List<Categoria> findByPadreIdAndActivaTrue(Long padreId);
}
