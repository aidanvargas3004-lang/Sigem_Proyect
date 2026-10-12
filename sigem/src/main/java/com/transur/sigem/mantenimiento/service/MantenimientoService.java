package com.transur.sigem.mantenimiento.service;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.common.util.UsuarioActual;
import com.transur.sigem.logistica.dto.MovimientoRequest;
import com.transur.sigem.logistica.service.MovimientoInventarioService;
import com.transur.sigem.mantenimiento.entity.PlanMantenimiento;
import com.transur.sigem.mantenimiento.repository.PlanMantenimientoRepository;
import com.transur.sigem.operaciones.entity.Incidencia;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.repository.IncidenciaRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import java.time.LocalDate;

import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.mantenimiento.dto.MantenimientoRequest;
import com.transur.sigem.mantenimiento.dto.MantenimientoResponse;
import com.transur.sigem.mantenimiento.dto.RepuestoUsadoDTO;
import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import com.transur.sigem.mantenimiento.entity.MantenimientoRepuesto;
import com.transur.sigem.mantenimiento.repository.MantenimientoRepository;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RepuestoRepository repuestoRepository;
    private final PlanMantenimientoRepository planRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final AsignacionRepository asignacionRepository;
    private final MovimientoInventarioService movimientoService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MantenimientoResponse crear(MantenimientoRequest request) {
        Maquinaria maq = maquinariaRepository.findById(request.getMaquinariaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Maquinaria", request.getMaquinariaId()));
        if (request.getFechaProgramada() == null) {
            throw new NegocioException("Indique la fecha programada del mantenimiento");
        }

        Mantenimiento mant = new Mantenimiento();
        mant.setMaquinaria(maq);

        if (request.getEmpleadoId() != null) {
            Empleado emp = empleadoRepository.findById(request.getEmpleadoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Empleado", request.getEmpleadoId()));
            mant.setEmpleado(emp);
        }

        mant.setTipoMantenimiento(request.getTipoMantenimiento());
        mant.setDescripcion(request.getDescripcion());
        mant.setFechaProgramada(request.getFechaProgramada());
        mant.setFechaInicio(request.getFechaInicio());
        mant.setFechaFin(request.getFechaFin());
        mant.setEstado(request.getEstado() != null ? request.getEstado() : "PROGRAMADO");
        mant.setObservaciones(request.getObservaciones());

        List<MantenimientoRepuesto> repuestosList = new ArrayList<>();
        BigDecimal costoTotal = BigDecimal.ZERO;

        if (request.getRepuestosUsados() != null) {
            for (RepuestoUsadoDTO dto : request.getRepuestosUsados()) {
                Repuesto rep = repuestoRepository.findById(dto.getRepuestoId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Repuesto", dto.getRepuestoId()));

                MantenimientoRepuesto mr = new MantenimientoRepuesto();
                mr.setMantenimiento(mant);
                mr.setRepuesto(rep);
                mr.setCantidad(dto.getCantidad());
                mr.setCostoUnitario(dto.getCostoUnitario());

                costoTotal = costoTotal.add(dto.getCostoUnitario().multiply(BigDecimal.valueOf(dto.getCantidad())));
                repuestosList.add(mr);

                // Descuenta stock reutilizando el servicio de logística (valida stock suficiente)
                registrarMovimiento(rep, dto.getCantidad(), "SALIDA", "Consumo en mantenimiento de " + maq.getCodigoInterno());
            }
        }

        mant.setCosto(costoTotal);
        mant.setRepuestosUsados(repuestosList);

        return mapToResponse(mantenimientoRepository.save(mant));
    }

    public List<MantenimientoResponse> listar() {
        return mantenimientoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MantenimientoResponse mapToResponse(Mantenimiento m) {
        String techName = null;
        if (m.getEmpleado() != null) {
            techName = m.getEmpleado().getNombres() + " " + m.getEmpleado().getApellidos();
        }

        return MantenimientoResponse.builder()
                .id(m.getId())
                .maquinariaCodigo(m.getMaquinaria().getCodigoInterno())
                .tecnicoNombre(techName)
                .tipoMantenimiento(m.getTipoMantenimiento())
                .descripcion(m.getDescripcion())
                .fechaProgramada(m.getFechaProgramada())
                .fechaInicio(m.getFechaInicio())
                .fechaFin(m.getFechaFin())
                .costoTotal(m.getCosto())
                .estado(m.getEstado())
                .cantidadRepuestos(m.getRepuestosUsados() != null ? m.getRepuestosUsados().size() : 0)
                .build();
    }

    // =====================================================================
    //  Ciclo de la orden: programada → en proceso → completada (RF013)
    // =====================================================================

    public List<Mantenimiento> listarOrdenes() {
        return mantenimientoRepository.findAllByOrderByIdDesc();
    }

    public Mantenimiento obtener(Long id) {
        return mantenimientoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Mantenimiento", id));
    }

    /** Ingreso a taller: la unidad queda EN_MANTENIMIENTO y no puede asignarse. */
    @Transactional
    public void iniciar(Long id) {
        Mantenimiento m = obtener(id);
        if (!"PROGRAMADO".equals(m.getEstado())) {
            throw new NegocioException("Solo se inician órdenes PROGRAMADAS");
        }
        Maquinaria maq = m.getMaquinaria();
        boolean trabajando = asignacionRepository.findByMaquinariaIdAndEstado(maq.getId(), "ACTIVA").stream()
                .anyMatch(a -> "EN_CURSO".equals(a.getServicio().getEstado()));
        if (trabajando) {
            throw new NegocioException("La unidad " + maq.getCodigoInterno() + " está trabajando en un servicio; ciérrelo primero");
        }
        m.setEstado("EN_PROCESO");
        m.setFechaInicio(LocalDate.now());
        maq.setEstadoOperativo("EN_MANTENIMIENTO");
    }

    /** Registra un repuesto consumido: sale del almacén y suma al costo de la orden. */
    @Transactional
    public void agregarRepuesto(Long id, Long repuestoId, Integer cantidad, BigDecimal costoUnitario) {
        Mantenimiento m = obtener(id);
        if (!"EN_PROCESO".equals(m.getEstado())) {
            throw new NegocioException("Los repuestos se registran con la orden EN PROCESO");
        }
        if (cantidad == null || cantidad <= 0 || costoUnitario == null || costoUnitario.signum() < 0) {
            throw new NegocioException("Indique una cantidad mayor que cero y un costo unitario válido");
        }
        Repuesto rep = repuestoRepository.findById(repuestoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Repuesto", repuestoId));
        registrarMovimiento(rep, cantidad, "SALIDA", "Consumo en orden MNT-" + m.getId());
        MantenimientoRepuesto mr = new MantenimientoRepuesto();
        mr.setMantenimiento(m);
        mr.setRepuesto(rep);
        mr.setCantidad(cantidad);
        mr.setCostoUnitario(costoUnitario);
        if (m.getRepuestosUsados() == null) {
            m.setRepuestosUsados(new ArrayList<>());
        }
        m.getRepuestosUsados().add(mr);
        m.setCosto(nvl(m.getCosto()).add(costoUnitario.multiply(BigDecimal.valueOf(cantidad))));
    }

    /** Cierra la orden, reinicia el plan preventivo y libera la unidad. */
    @Transactional
    public void completar(Long id, String trabajoRealizado, BigDecimal costoManoObra, BigDecimal horometro) {
        Mantenimiento m = obtener(id);
        if (!"EN_PROCESO".equals(m.getEstado())) {
            throw new NegocioException("Solo se completan órdenes EN PROCESO");
        }
        if (trabajoRealizado == null || trabajoRealizado.isBlank()) {
            throw new NegocioException("Describa el trabajo realizado");
        }
        Maquinaria maq = m.getMaquinaria();
        if (horometro != null) {
            if (horometro.compareTo(nvl(maq.getHorometroActual())) < 0) {
                throw new NegocioException("El horómetro no puede ser menor que el actual (" + nvl(maq.getHorometroActual()) + " h)");
            }
            maq.setHorometroActual(horometro);
        }
        m.setHorometroRegistro(nvl(maq.getHorometroActual()));
        m.setCosto(nvl(m.getCosto()).add(nvl(costoManoObra)));
        m.setObservaciones((m.getObservaciones() != null ? m.getObservaciones() + "\n" : "") + "Trabajo realizado: " + trabajoRealizado.trim());
        m.setFechaFin(LocalDate.now());
        m.setEstado("COMPLETADO");
        PlanMantenimiento plan = m.getPlan();
        if (plan != null) {
            plan.setUltimaFecha(LocalDate.now());
            plan.setUltimoValor(plan.porFecha() ? null : plan.lecturaActual());
        }
        if (m.getIncidencia() != null) {
            m.getIncidencia().setEstado("RESUELTA");
        }
        boolean asignada = !asignacionRepository.findByMaquinariaIdAndEstado(maq.getId(), "ACTIVA").isEmpty();
        maq.setEstadoOperativo(asignada ? "EN_SERVICIO" : "OPERATIVO");
    }

    @Transactional
    public void cancelar(Long id) {
        Mantenimiento m = obtener(id);
        if (!"PROGRAMADO".equals(m.getEstado())) {
            throw new NegocioException("Solo se cancelan órdenes PROGRAMADAS");
        }
        m.setEstado("CANCELADO");
    }

    /** RF011 → RF013: orden correctiva generada a partir de una incidencia. */
    @Transactional
    public Mantenimiento generarDesdeIncidencia(Long incidenciaId) {
        Incidencia inc = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Incidencia", incidenciaId));
        Maquinaria maq = inc.getMaquinaria();
        if (maq == null) {
            maq = asignacionRepository.findByServicioId(inc.getServicio().getId()).stream()
                    .map(com.transur.sigem.operaciones.entity.Asignacion::getMaquinaria).findFirst()
                    .orElseThrow(() -> new NegocioException("La incidencia no indica la unidad afectada"));
        }
        Mantenimiento m = new Mantenimiento();
        m.setMaquinaria(maq);
        m.setTipoMantenimiento("CORRECTIVO");
        m.setDescripcion("Atención de la incidencia INC-" + inc.getId() + " (" + inc.getTipo() + "): " + inc.getDescripcion());
        m.setFechaProgramada(LocalDate.now());
        m.setEstado("PROGRAMADO");
        m.setIncidencia(inc);
        inc.setEstado("EN_PROCESO");
        return mantenimientoRepository.save(m);
    }

    // =====================================================================
    //  Planes de mantenimiento preventivo (RF012)
    // =====================================================================

    public List<PlanMantenimiento> listarPlanes() {
        return planRepository.findAll();
    }

    @Transactional
    public PlanMantenimiento crearPlan(Long maquinariaId, String actividad, String criterio, BigDecimal frecuencia,
                                      BigDecimal ultimoValor, LocalDate ultimaFecha) {
        Maquinaria maq = maquinariaRepository.findById(maquinariaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Maquinaria", maquinariaId));
        if (!java.util.List.of(PlanMantenimiento.HOROMETRO, PlanMantenimiento.KILOMETRAJE, PlanMantenimiento.FECHA).contains(criterio)) {
            throw new NegocioException("Criterio no válido: use HOROMETRO, KILOMETRAJE o FECHA");
        }
        if (frecuencia == null || frecuencia.signum() <= 0) {
            throw new NegocioException("La frecuencia debe ser mayor que cero");
        }
        PlanMantenimiento p = new PlanMantenimiento();
        p.setMaquinaria(maq);
        p.setActividad(actividad == null || actividad.isBlank() ? "Mantenimiento preventivo" : actividad.trim());
        p.setCriterio(criterio);
        p.setFrecuencia(frecuencia);
        p.setUltimoValor(PlanMantenimiento.FECHA.equals(criterio) ? null
                : (ultimoValor != null ? ultimoValor : PlanMantenimiento.KILOMETRAJE.equals(criterio)
                        ? nvl(maq.getKilometrajeActual()) : nvl(maq.getHorometroActual())));
        p.setUltimaFecha(ultimaFecha != null ? ultimaFecha : LocalDate.now());
        return planRepository.save(p);
    }

    /** Programa la orden preventiva que corresponde a un plan próximo o vencido. */
    @Transactional
    public Mantenimiento generarDesdePlan(Long planId) {
        PlanMantenimiento p = planRepository.findById(planId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de mantenimiento", planId));
        Mantenimiento m = new Mantenimiento();
        m.setMaquinaria(p.getMaquinaria());
        m.setTipoMantenimiento("PREVENTIVO");
        m.setDescripcion(p.getActividad() + " (plan cada " + p.getFrecuencia().stripTrailingZeros().toPlainString() + " "
                + p.getUnidad() + ")");
        m.setFechaProgramada(LocalDate.now().plusDays(1));
        m.setEstado("PROGRAMADO");
        m.setPlan(p);
        return mantenimientoRepository.save(m);
    }

    @Transactional
    public void cambiarEstadoPlan(Long planId) {
        PlanMantenimiento p = planRepository.findById(planId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan de mantenimiento", planId));
        p.setActivo(!Boolean.TRUE.equals(p.getActivo()));
    }

    private void registrarMovimiento(Repuesto rep, Integer cantidad, String tipo, String motivo) {
        MovimientoRequest mr = new MovimientoRequest();
        mr.setRepuestoId(rep.getId());
        mr.setCantidad(cantidad);
        mr.setTipoMovimiento(tipo);
        mr.setMotivo(motivo);
        UsuarioActual.username().flatMap(usuarioRepository::findByUsername).ifPresent(u -> mr.setUsuarioId(u.getId()));
        movimientoService.registrar(mr);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
