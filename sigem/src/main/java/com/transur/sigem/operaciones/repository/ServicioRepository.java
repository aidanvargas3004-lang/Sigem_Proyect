package com.transur.sigem.operaciones.repository;

import com.transur.sigem.operaciones.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {

    // ✅ ESTE ES EL MÉTODO QUE FALTABA
    List<Servicio> findByContratoId(Long contratoId);
    List<Servicio> findByContratoClienteId(Long clienteId);
}