package com.library.repository;

import com.library.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    boolean existsByIsbn(String isbn);
    Optional<Libro> findByIsbn(String isbn);

    @Query("SELECT DISTINCT l FROM Libro l LEFT JOIN l.autores a LEFT JOIN l.categorias c WHERE l.activo = true AND " +
            "(:titulo IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', CAST(:titulo AS string), '%'))) AND " +
            "(:isbn IS NULL OR l.isbn = :isbn) AND " +
            "(:autorId IS NULL OR a.id = :autorId) AND " +
            "(:categoriaId IS NULL OR c.id = :categoriaId) AND " +
            "(:editorial IS NULL OR LOWER(l.editorial) LIKE LOWER(CONCAT('%', CAST(:editorial AS string), '%')))")
    Page<Libro> busquedaAvanzada(
            @Param("titulo") String titulo,
            @Param("isbn") String isbn,
            @Param("autorId") Long autorId,
            @Param("categoriaId") Long categoriaId,
            @Param("editorial") String editorial,
            Pageable pageable);

    @Query("SELECT l FROM Libro l JOIN l.categorias c WHERE c.id IN :categoriaIds AND l.activo = true AND l.id != :libroId")
    List<Libro> findByCategoriaIdsAndNotId(@Param("categoriaIds") List<Long> categoriaIds,
                                           @Param("libroId") Long libroId,
                                           Pageable pageable);

    @Query("SELECT l FROM Libro l JOIN l.categorias c WHERE c.id IN :categoriaIds AND l.activo = true")
    List<Libro> findByCategoriaIds(@Param("categoriaIds") List<Long> categoriaIds, Pageable pageable);
}