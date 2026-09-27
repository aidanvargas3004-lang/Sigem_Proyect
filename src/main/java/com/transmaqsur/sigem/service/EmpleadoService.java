package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.enums.CargoEmpleado;
import com.transmaqsur.sigem.model.enums.EstadoEmpleado;
import com.transmaqsur.sigem.repository.AsignacionRepository;
import com.transmaqsur.sigem.repository.EmpleadoRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final AsignacionRepository asignacionRepository;

    @Transactional(readOnly = true)
    public List<Empleado> listar() {
        return empleadoRepository.findAllByOrderByApellidosAsc();
    }

    @Transactional(readOnly = true)
    public List<Empleado> listarActivos() {
        return empleadoRepository.findByEstadoOrderByApellidosAsc(EstadoEmpleado.ACTIVO);
    }

    /** Conductores y operadores activos. */
    @Transactional(readOnly = true)
    public List<Empleado> listarPersonalDeCampo() {
        return empleadoRepository.findByCargoInAndEstadoOrderByApellidosAsc(
                List.of(CargoEmpleado.CONDUCTOR, CargoEmpleado.OPERADOR), EstadoEmpleado.ACTIVO);
    }

    @Transactional(readOnly = true)
    public List<Empleado> listarTecnicos() {
        return empleadoRepository.findByCargoInAndEstadoOrderByApellidosAsc(
                List.of(CargoEmpleado.TECNICO, CargoEmpleado.JEFE_MANTENIMIENTO), EstadoEmpleado.ACTIVO);
    }

    @Transactional(readOnly = true)
    public Empleado obtener(Long id) {
        return empleadoRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Empleado", id));
    }

    public Empleado guardar(Empleado form) {
        Long id = form.isNuevo() ? 0L : form.getId();
        if (empleadoRepository.existsByDniAndIdNot(form.getDni(), id)) {
            throw new NegocioException("Ya existe un empleado con DNI " + form.getDni());
        }
        if (form.isPersonalDeCampo() && (form.getLicenciaNumero() == null || form.getLicenciaNumero().isBlank()
                || form.getLicenciaVencimiento() == null)) {
            throw new NegocioException("Los conductores y operadores deben registrar su licencia y la fecha de vencimiento");
        }
        if (!form.isNuevo() && form.getEstado() != EstadoEmpleado.ACTIVO
                && !asignacionRepository.findByEmpleadoIdAndEstadoInOrderByFechaInicioAsc(form.getId(), EstadosActivos.ASIGNACION).isEmpty()) {
            throw new NegocioException("El empleado tiene asignaciones programadas o en curso; reasígnelas antes de cambiar su estado a "
                    + form.getEstado().getLabel());
        }
        if (form.isNuevo()) {
            return empleadoRepository.save(form);
        }
        Empleado e = obtener(form.getId());
        Entidades.copiar(form, e);
        return e;
    }
}
