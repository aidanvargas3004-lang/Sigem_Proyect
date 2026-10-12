package com.transur.sigem.compras.repository;

import com.transur.sigem.compras.entity.RecepcionCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RecepcionCompraRepository extends JpaRepository<RecepcionCompra, Long> {

    Optional<RecepcionCompra> findByNumero(String numero);
    boolean existsByNumero(String numero);

    // ✅ ESTE ES EL MÉTODO QUE FALTABA
    List<RecepcionCompra> findByOrdenCompraId(Long ordenCompraId);
}