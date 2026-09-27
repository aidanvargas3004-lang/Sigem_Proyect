package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Cotizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoCotizacion;

import java.util.List;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    List<Cotizacion> findAllByOrderByIdDesc();

    List<Cotizacion> findBySolicitudIdOrderByIdDesc(Long solicitudId);

    List<Cotizacion> findByClienteIdOrderByIdDesc(Long clienteId);

    List<Cotizacion> findByEstado(EstadoCotizacion estado);
}
