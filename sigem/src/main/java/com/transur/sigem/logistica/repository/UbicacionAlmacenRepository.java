package com.transur.sigem.logistica.repository;

import com.transur.sigem.logistica.entity.UbicacionAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UbicacionAlmacenRepository extends JpaRepository<UbicacionAlmacen, Long> {
    List<UbicacionAlmacen> findByAlmacenId(Long almacenId);
    Optional<UbicacionAlmacen> findByCodigoAndAlmacenId(String codigo, Long almacenId);
}