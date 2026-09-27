package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoCompra;

import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findAllByOrderByIdDesc();

    List<Compra> findByProveedorIdOrderByIdDesc(Long proveedorId);

    long countByEstado(EstadoCompra estado);
}
