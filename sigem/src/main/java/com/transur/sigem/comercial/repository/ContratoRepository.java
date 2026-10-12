package com.transur.sigem.comercial.repository;

import com.transur.sigem.comercial.entity.Contrato;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
    @EntityGraph(attributePaths = {"cliente", "cotizacion", "cotizacion.solicitud"})
    List<Contrato> findAllByOrderByFechaInicioDesc();

    @EntityGraph(attributePaths = {"cliente", "cotizacion", "cotizacion.solicitud"})
    Optional<Contrato> findById(Long id);

    @EntityGraph(attributePaths = {"cliente", "cotizacion", "cotizacion.solicitud"})
    List<Contrato> findByEstadoOrderByFechaInicioDesc(String estado);

    @EntityGraph(attributePaths = {"cliente", "cotizacion", "cotizacion.solicitud"})
    List<Contrato> findByTipoContratoOrderByFechaInicioDesc(String tipoContrato);

    Optional<Contrato> findByNumero(String numero);
    boolean existsByNumero(String numero);
    boolean existsByCotizacionId(Long cotizacionId);
    @EntityGraph(attributePaths = {"cliente", "cotizacion", "cotizacion.solicitud"})
    List<Contrato> findByClienteIdOrderByFechaInicioDesc(Long clienteId);
}
