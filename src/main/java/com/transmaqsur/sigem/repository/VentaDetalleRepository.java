package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.VentaDetalle;
import com.transmaqsur.sigem.model.enums.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {

    boolean existsByAlquilerIdAndVentaEstadoNot(Long alquilerId, EstadoVenta estado);

    boolean existsByServicioIdAndVentaEstadoNot(Long servicioId, EstadoVenta estado);
}
