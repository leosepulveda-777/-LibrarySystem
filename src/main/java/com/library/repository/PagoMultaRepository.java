package com.library.repository;

import com.library.entity.PagoMulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagoMultaRepository extends JpaRepository<PagoMulta, Long> {
    List<PagoMulta> findByMultaIdOrderByFechaPagoDesc(Long multaId);
}
