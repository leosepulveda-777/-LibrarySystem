package com.library.repository;

import com.library.entity.Autor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Autor a WHERE a.activo = true AND " +
           "(:search IS NULL OR LOWER(a.nombre) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(a.apellido) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Autor> findBySearchTerm(@Param("search") String search, Pageable pageable);
}
