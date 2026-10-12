package com.transur.sigem.maquinaria.repository;

import com.transur.sigem.maquinaria.entity.TipoMaquinaria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TipoMaquinariaRepository extends JpaRepository<TipoMaquinaria, Long> {
    Optional<TipoMaquinaria> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}