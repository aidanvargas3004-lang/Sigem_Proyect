package com.transur.sigem.logistica.repository;

import com.transur.sigem.logistica.entity.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {
    Optional<Repuesto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}