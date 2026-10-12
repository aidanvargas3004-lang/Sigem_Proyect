package com.transur.sigem.comercial.repository;
import com.transur.sigem.comercial.entity.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByClienteId(Long clienteId);
    List<Solicitud> findByEstado(String estado);
}