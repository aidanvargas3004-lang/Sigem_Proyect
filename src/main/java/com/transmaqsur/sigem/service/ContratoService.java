package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.model.Cotizacion;
import com.transmaqsur.sigem.model.enums.EstadoContrato;
import com.transmaqsur.sigem.model.enums.EstadoCotizacion;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.EstadoSolicitud;
import com.transmaqsur.sigem.repository.AlquilerRepository;
import com.transmaqsur.sigem.repository.ContratoRepository;
import com.transmaqsur.sigem.repository.ServicioTransporteRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Contratos con clientes: generación desde cotización, renovación y cierre. */
@Service
@RequiredArgsConstructor
@Transactional
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final CotizacionService cotizacionService;
    private final AlquilerRepository alquilerRepository;
    private final ServicioTransporteRepository servicioRepository;

    @Transactional(readOnly = true)
    public List<Contrato> listar() {
        return contratoRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Contrato> listarVigentes() {
        return contratoRepository.findByEstadoOrderByFechaFinAsc(EstadoContrato.VIGENTE);
    }

    @Transactional(readOnly = true)
    public Contrato obtener(Long id) {
        return contratoRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Contrato", id));
    }

    public Contrato guardar(Contrato form) {
        if (form.getFechaFin().isBefore(form.getFechaInicio())) {
            throw new NegocioException("La fecha de fin del contrato no puede ser anterior a la de inicio");
        }
        if (form.isNuevo()) {
            form.setEstado(EstadoContrato.VIGENTE);
            Contrato c = contratoRepository.save(form);
            c.setCodigo(Entidades.codigo("CTR", c.getId()));
            return c;
        }
        Contrato c = obtener(form.getId());
        if (c.getEstado() != EstadoContrato.VIGENTE) {
            throw new NegocioException("Solo se pueden editar contratos vigentes");
        }
        Entidades.copiar(form, c, "codigo", "estado", "cliente", "cotizacion", "contratoAnterior");
        return c;
    }

    /** Genera el contrato a partir de una cotización aceptada. */
    public Contrato generarDesdeCotizacion(Long cotizacionId) {
        Cotizacion cot = cotizacionService.obtener(cotizacionId);
        if (cot.getEstado() != EstadoCotizacion.ACEPTADA) {
            throw new NegocioException("Solo se generan contratos desde cotizaciones aceptadas");
        }
        contratoRepository.findFirstByCotizacionId(cotizacionId).ifPresent(c -> {
            throw new NegocioException("La cotización ya tiene el contrato " + c.getCodigo());
        });
        Contrato c = new Contrato();
        c.setCliente(cot.getCliente());
        c.setCotizacion(cot);
        c.setMontoTotal(cot.getTotal());
        c.setCondiciones(cot.getCondiciones());
        if (cot.getSolicitud() != null) {
            c.setFechaInicio(cot.getSolicitud().getFechaInicio());
            c.setFechaFin(cot.getSolicitud().getFechaFin());
            c.setObjeto(cot.getSolicitud().getTipoServicio().getLabel());
            cot.getSolicitud().setEstado(EstadoSolicitud.ATENDIDA);
        } else {
            c.setFechaInicio(LocalDate.now());
            c.setFechaFin(LocalDate.now().plusMonths(1));
            c.setObjeto("Según cotización " + cot.getCodigo());
        }
        if (c.getFechaFin().isBefore(LocalDate.now())) {
            c.setFechaFin(LocalDate.now().plusMonths(1));
        }
        return guardar(c);
    }

    /** Renueva el contrato: crea uno nuevo enlazado y marca el anterior como renovado. */
    public Contrato renovar(Long id, LocalDate nuevaFechaFin, BigDecimal nuevoMonto) {
        Contrato anterior = obtener(id);
        if (anterior.getEstado() != EstadoContrato.VIGENTE && anterior.getEstado() != EstadoContrato.FINALIZADO) {
            throw new NegocioException("Solo se renuevan contratos vigentes o finalizados");
        }
        LocalDate inicio = anterior.getFechaFin().isBefore(LocalDate.now()) ? LocalDate.now() : anterior.getFechaFin().plusDays(1);
        if (nuevaFechaFin == null || !nuevaFechaFin.isAfter(inicio)) {
            throw new NegocioException("La nueva fecha de fin debe ser posterior al " + inicio);
        }
        Contrato nuevo = new Contrato();
        nuevo.setCliente(anterior.getCliente());
        nuevo.setContratoAnterior(anterior);
        nuevo.setObjeto(anterior.getObjeto());
        nuevo.setCondiciones(anterior.getCondiciones());
        nuevo.setFechaInicio(inicio);
        nuevo.setFechaFin(nuevaFechaFin);
        nuevo.setMontoTotal(nuevoMonto != null ? nuevoMonto : anterior.getMontoTotal());
        anterior.setEstado(EstadoContrato.RENOVADO);
        return guardar(nuevo);
    }

    public void finalizar(Long id) {
        Contrato c = obtener(id);
        if (c.getEstado() != EstadoContrato.VIGENTE) {
            throw new NegocioException("El contrato no está vigente");
        }
        boolean activas = alquilerRepository.findByContratoIdOrderByIdDesc(id).stream().anyMatch(a -> activa(a.getEstado()))
                || servicioRepository.findByContratoIdOrderByIdDesc(id).stream().anyMatch(s -> activa(s.getEstado()));
        if (activas) {
            throw new NegocioException("El contrato tiene alquileres o servicios programados o en curso");
        }
        c.setEstado(EstadoContrato.FINALIZADO);
    }

    public void anular(Long id) {
        Contrato c = obtener(id);
        if (c.getEstado() != EstadoContrato.VIGENTE) {
            throw new NegocioException("Solo se anulan contratos vigentes");
        }
        if (!alquilerRepository.findByContratoIdOrderByIdDesc(id).isEmpty() || !servicioRepository.findByContratoIdOrderByIdDesc(id).isEmpty()) {
            throw new NegocioException("El contrato ya tiene operaciones registradas; finalícelo en lugar de anularlo");
        }
        c.setEstado(EstadoContrato.ANULADO);
    }

    private static boolean activa(EstadoOperacion e) {
        return e == EstadoOperacion.PROGRAMADO || e == EstadoOperacion.EN_CURSO;
    }
}
