package com.transur.sigem.operaciones.repository;

import com.transur.sigem.operaciones.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    // Listar incidencias por servicio (para ver historial de un servicio específico)
    List<Incidencia> findByServicioId(Long servicioId);

    // Listar incidencias por maquinaria (para ver historial de fallas de una máquina)
    List<Incidencia> findByMaquinariaId(Long maquinariaId);

    // Listar incidencias abiertas/pendientes
    List<Incidencia> findByEstado(String estado);
}