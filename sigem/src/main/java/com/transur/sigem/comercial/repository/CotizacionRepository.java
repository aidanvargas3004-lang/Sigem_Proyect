package com.transur.sigem.comercial.repository;

import com.transur.sigem.comercial.entity.Cotizacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {
    Optional<Cotizacion> findByNumero(String numero);
    boolean existsByNumero(String numero);
    boolean existsBySolicitudId(Long solicitudId);
    @EntityGraph(attributePaths = {"cliente", "solicitud", "detalles", "detalles.maquinaria"})
    List<Cotizacion> findByClienteIdOrderByFechaEmisionDesc(Long clienteId);

    @Override
    @EntityGraph(attributePaths = {"cliente", "solicitud"})
    List<Cotizacion> findAll();
}
