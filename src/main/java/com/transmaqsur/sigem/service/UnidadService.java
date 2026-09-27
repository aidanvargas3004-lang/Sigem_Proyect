package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoAsignacion;
import com.transmaqsur.sigem.model.enums.EstadoOrden;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.AsignacionRepository;
import com.transmaqsur.sigem.repository.OrdenMantenimientoRepository;
import com.transmaqsur.sigem.repository.UnidadRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** Maquinaria y flota: registro de unidades, lecturas y estado consolidado. */
@Service
@RequiredArgsConstructor
@Transactional
public class UnidadService {

    private final UnidadRepository unidadRepository;
    private final AsignacionRepository asignacionRepository;
    private final OrdenMantenimientoRepository ordenRepository;

    @Transactional(readOnly = true)
    public List<Unidad> listar() {
        return unidadRepository.findAllByOrderByCodigoAsc();
    }

    /** Unidades operativas (excluye las que están fuera de servicio). */
    @Transactional(readOnly = true)
    public List<Unidad> listarOperativas() {
        return unidadRepository.findByEstadoNotOrderByCodigoAsc(EstadoUnidad.FUERA_SERVICIO);
    }

    @Transactional(readOnly = true)
    public List<Unidad> listarVehiculos() {
        return unidadRepository.findByTipoInAndEstadoNotOrderByCodigoAsc(
                Arrays.stream(TipoUnidad.values()).filter(TipoUnidad::isVehiculo).toList(), EstadoUnidad.FUERA_SERVICIO);
    }

    @Transactional(readOnly = true)
    public Unidad obtener(Long id) {
        return unidadRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Unidad", id));
    }

    public Unidad guardar(Unidad form) {
        form.setCodigo(form.getCodigo().trim().toUpperCase());
        if (form.getPlaca() != null) {
            form.setPlaca(form.getPlaca().trim().toUpperCase());
        }
        if (form.getTipo().isVehiculo() && (form.getPlaca() == null || form.getPlaca().isBlank())) {
            throw new NegocioException("Los vehículos (" + form.getTipo().getLabel() + ") deben tener placa");
        }
        Long id = form.isNuevo() ? 0L : form.getId();
        if (unidadRepository.existsByCodigoAndIdNot(form.getCodigo(), id)) {
            throw new NegocioException("Ya existe una unidad con el código " + form.getCodigo());
        }
        if (form.isNuevo()) {
            form.setEstado(EstadoUnidad.DISPONIBLE);
            if (form.getLecturaUltimoMantenimiento().compareTo(form.getLecturaActual()) > 0) {
                throw new NegocioException("La lectura del último mantenimiento no puede ser mayor que la lectura actual");
            }
            return unidadRepository.save(form);
        }
        Unidad u = obtener(form.getId());
        // El estado y las lecturas se actualizan solo mediante operaciones y mantenimientos
        Entidades.copiar(form, u, "estado", "lecturaActual", "lecturaUltimoMantenimiento");
        return u;
    }

    /** Registra una nueva lectura de odómetro/horómetro (no puede retroceder). */
    public void actualizarLectura(Unidad u, BigDecimal nuevaLectura) {
        if (nuevaLectura == null) {
            return;
        }
        if (nuevaLectura.compareTo(u.getLecturaActual()) < 0) {
            throw new NegocioException("La lectura " + nuevaLectura.toPlainString() + " " + u.getTipo().getMedida()
                    + " es menor que la lectura actual de " + u.getCodigo() + " (" + u.getLecturaActual().toPlainString() + ")");
        }
        u.setLecturaActual(nuevaLectura);
    }

    public void registrarLectura(Long id, BigDecimal lectura) {
        actualizarLectura(obtener(id), lectura);
    }

    /** Da de baja temporal (fuera de servicio) o reactiva la unidad. */
    public void cambiarFueraDeServicio(Long id) {
        Unidad u = obtener(id);
        if (u.getEstado() == EstadoUnidad.FUERA_SERVICIO) {
            u.setEstado(EstadoUnidad.DISPONIBLE);
            recalcularEstado(u);
            return;
        }
        if (asignacionRepository.countByUnidadIdAndEstadoIn(u.getId(), EstadosActivos.ASIGNACION) > 0) {
            throw new NegocioException("La unidad tiene asignaciones programadas o en curso; cancélelas o finalícelas primero");
        }
        if (ordenRepository.existsByUnidadIdAndEstadoIn(u.getId(), List.of(EstadoOrden.EN_PROCESO))) {
            throw new NegocioException("La unidad tiene un mantenimiento en proceso");
        }
        u.setEstado(EstadoUnidad.FUERA_SERVICIO);
    }

    /**
     * Estado consolidado de la unidad según sus asignaciones y mantenimientos:
     * mantenimiento en proceso &gt; en servicio &gt; asignada &gt; disponible.
     */
    public void recalcularEstado(Unidad u) {
        if (u.getEstado() == EstadoUnidad.FUERA_SERVICIO) {
            return;
        }
        EstadoUnidad nuevo;
        if (ordenRepository.existsByUnidadIdAndEstadoIn(u.getId(), List.of(EstadoOrden.EN_PROCESO))) {
            nuevo = EstadoUnidad.EN_MANTENIMIENTO;
        } else if (asignacionRepository.countByUnidadIdAndEstadoIn(u.getId(), List.of(EstadoAsignacion.EN_CURSO)) > 0) {
            nuevo = EstadoUnidad.EN_SERVICIO;
        } else if (asignacionRepository.countByUnidadIdAndEstadoIn(u.getId(), List.of(EstadoAsignacion.PROGRAMADA)) > 0) {
            nuevo = EstadoUnidad.ASIGNADA;
        } else {
            nuevo = EstadoUnidad.DISPONIBLE;
        }
        u.setEstado(nuevo);
    }

    @Transactional(readOnly = true)
    public List<Unidad> requierenMantenimiento() {
        return listarOperativas().stream().filter(Unidad::isRequiereMantenimiento).toList();
    }
}
