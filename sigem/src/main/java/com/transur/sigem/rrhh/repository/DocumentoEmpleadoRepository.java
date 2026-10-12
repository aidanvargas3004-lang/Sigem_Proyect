package com.transur.sigem.rrhh.repository;

import com.transur.sigem.rrhh.entity.DocumentoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DocumentoEmpleadoRepository extends JpaRepository<DocumentoEmpleado, Long> {
    List<DocumentoEmpleado> findByEmpleadoId(Long empleadoId);

    // CRÍTICO: Documentos próximos a vencer (para alertas automáticas)
    List<DocumentoEmpleado> findByFechaVencimientoBetweenAndEstadoTrue(
            LocalDate inicio, LocalDate fin);

    // Documentos vencidos
    List<DocumentoEmpleado> findByFechaVencimientoBeforeAndEstadoTrue(LocalDate fecha);
}