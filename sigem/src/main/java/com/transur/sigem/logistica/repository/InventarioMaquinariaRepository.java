package com.transur.sigem.logistica.repository;

import com.transur.sigem.logistica.entity.InventarioMaquinaria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InventarioMaquinariaRepository extends JpaRepository<InventarioMaquinaria, Long> {
    Optional<InventarioMaquinaria> findByMaquinariaId(Long maquinariaId);
}