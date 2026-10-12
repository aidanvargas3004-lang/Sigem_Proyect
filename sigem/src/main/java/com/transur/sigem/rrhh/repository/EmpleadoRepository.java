package com.transur.sigem.rrhh.repository;

import com.transur.sigem.rrhh.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByDocumento(String documento);
    boolean existsByDocumento(String documento);

    // Búsquedas útiles para filtros
    List<Empleado> findByCargo(String cargo);
    List<Empleado> findByEstadoTrue(); // Solo activos
}