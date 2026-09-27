package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findAllByOrderByRazonSocialAsc();

    List<Proveedor> findByActivoTrueOrderByRazonSocialAsc();

    boolean existsByRucAndIdNot(String ruc, Long id);

    boolean existsByRuc(String ruc);
}
