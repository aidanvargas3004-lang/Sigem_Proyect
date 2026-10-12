package com.transur.sigem.activos.repository;

import com.transur.sigem.activos.entity.DetalleAdquisicion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleAdquisicionRepository extends JpaRepository<DetalleAdquisicion, Long> {
}