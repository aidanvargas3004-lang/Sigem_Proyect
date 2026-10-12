package com.transur.sigem.compras.repository;

import com.transur.sigem.compras.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    Optional<OrdenCompra> findByNumero(String numero);
    boolean existsByNumero(String numero);
}