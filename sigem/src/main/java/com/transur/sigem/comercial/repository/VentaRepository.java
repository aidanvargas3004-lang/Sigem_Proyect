package com.transur.sigem.comercial.repository;
import com.transur.sigem.comercial.entity.Venta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByNumero(String numero);
    boolean existsByNumero(String numero);

    @Override
    @EntityGraph(attributePaths = {"contrato", "contrato.cliente"})
    List<Venta> findAll();
}
