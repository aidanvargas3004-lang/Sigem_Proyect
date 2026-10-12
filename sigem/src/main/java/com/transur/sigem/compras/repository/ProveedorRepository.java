package com.transur.sigem.compras.repository;

import com.transur.sigem.compras.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
}