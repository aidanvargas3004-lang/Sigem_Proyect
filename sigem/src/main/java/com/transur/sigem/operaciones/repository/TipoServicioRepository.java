package com.transur.sigem.operaciones.repository;

import com.transur.sigem.operaciones.entity.TipoServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TipoServicioRepository extends JpaRepository<TipoServicio, Long> {
    Optional<TipoServicio> findByNombre(String nombre);
}