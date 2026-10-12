package com.transur.sigem.operaciones.repository;

import com.transur.sigem.operaciones.entity.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    // Buscar asignaciones por servicio
    List<Asignacion> findByServicioId(Long servicioId);

    // ✅ ESTE ES EL MÉTODO QUE FALTABA
    List<Asignacion> findByMaquinariaId(Long maquinariaId);

    // Buscar asignaciones activas de un empleado específico
    List<Asignacion> findByEmpleadoIdAndEstado(Long empleadoId, String estado);

    List<Asignacion> findByMaquinariaIdAndEstado(Long maquinariaId, String estado);
}