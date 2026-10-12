package com.transur.sigem.logistica.repository;

import com.transur.sigem.logistica.entity.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    Optional<Almacen> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}