package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoSolicitud;

import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findAllByOrderByIdDesc();

    List<Solicitud> findByClienteIdOrderByIdDesc(Long clienteId);

    List<Solicitud> findByEstadoInOrderByIdDesc(List<EstadoSolicitud> estados);

    long countByEstado(EstadoSolicitud estado);
}
