package com.transur.sigem.logistica.repository;

import com.transur.sigem.logistica.entity.CategoriaRepuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoriaRepuestoRepository extends JpaRepository<CategoriaRepuesto, Long> {
    Optional<CategoriaRepuesto> findByNombre(String nombre);
}