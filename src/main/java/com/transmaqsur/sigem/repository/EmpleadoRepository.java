package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.CargoEmpleado;
import com.transmaqsur.sigem.model.enums.EstadoEmpleado;

import java.time.LocalDate;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    List<Empleado> findAllByOrderByApellidosAsc();

    List<Empleado> findByEstadoOrderByApellidosAsc(EstadoEmpleado estado);

    List<Empleado> findByCargoInAndEstadoOrderByApellidosAsc(List<CargoEmpleado> cargos, EstadoEmpleado estado);

    List<Empleado> findByEstadoAndLicenciaVencimientoBefore(EstadoEmpleado estado, LocalDate fecha);

    boolean existsByDniAndIdNot(String dni, Long id);

    boolean existsByDni(String dni);
}
