package com.transur.sigem.gerencia.controller;

import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.entity.Venta;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.comercial.repository.VentaRepository;
import com.transur.sigem.compras.entity.OrdenCompra;
import com.transur.sigem.compras.repository.OrdenCompraRepository;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import com.transur.sigem.mantenimiento.repository.MantenimientoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.marketing.entity.Oportunidad;
import com.transur.sigem.marketing.repository.OportunidadRepository;
import com.transur.sigem.operaciones.entity.Incidencia;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.repository.IncidenciaRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import com.transur.sigem.cliente.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gerencia")
@RequiredArgsConstructor
public class GerenciaViewController {

    private final ContratoRepository contratoRepository;
    private final VentaRepository ventaRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final ServicioRepository servicioRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final DocumentoEmpleadoRepository documentoRepository;
    private final ClienteRepository clienteRepository;
    private final RepuestoRepository repuestoRepository;
    private final OportunidadRepository oportunidadRepository;

    @GetMapping({"", "/", "/dashboard", "/deshboard"})
    public String dashboard(Model model) {
        Resumen r = construirResumen();
        model.addAttribute("active", "dashboard");
        agregarResumen(model, r);
        model.addAttribute("contratosRecientes", contratosRecientes());
        model.addAttribute("serviciosRecientes", serviciosRecientes());
        model.addAttribute("alertas", construirAlertas());
        model.addAttribute("topOportunidades", topOportunidades());
        return "gerencia/dashboard";
    }

    @GetMapping("/indicadores")
    public String indicadores(Model model) {
        Resumen r = construirResumen();
        model.addAttribute("active", "indicadores");
        agregarResumen(model, r);
        model.addAttribute("serviciosPorEstado", agrupar(servicioRepository.findAll(), Servicio::getEstado));
        model.addAttribute("contratosPorTipo", agrupar(contratoRepository.findAllByOrderByFechaInicioDesc(), Contrato::getTipoContrato));
        model.addAttribute("maquinariaPorEstado", agrupar(maquinariaRepository.findAll(), Maquinaria::getEstadoOperativo));
        model.addAttribute("empleadosPorCargo", agrupar(empleadoRepository.findAll(), Empleado::getCargo));
        model.addAttribute("repuestosCriticos", repuestosCriticos());
        return "gerencia/indicadores";
    }

    @GetMapping("/rentabilidad")
    public String rentabilidad(Model model) {
        Resumen r = construirResumen();
        model.addAttribute("active", "rentabilidad");
        agregarResumen(model, r);
        model.addAttribute("ingresosVentas", r.ingresosVentas);
        model.addAttribute("ingresosServiciosAlquiler", r.ingresosServiciosAlquiler);
        model.addAttribute("costosCompras", r.costosCompras);
        model.addAttribute("costosMantenimiento", r.costosMantenimiento);
        model.addAttribute("resultadoOperativo", r.resultadoOperativo);
        model.addAttribute("margen", r.margen);
        model.addAttribute("ingresosMes", r.ingresosMes);
        model.addAttribute("costosMes", r.costosMes);
        model.addAttribute("resultadoMes", r.resultadoMes);
        model.addAttribute("notaRentabilidad", "El resultado es un indicador gerencial estimado con los ingresos contractuales y costos registrados en SIGEM; no reemplaza la contabilidad financiera.");
        return "gerencia/rentabilidad";
    }

    @GetMapping("/operaciones")
    public String operaciones(Model model) {
        Resumen r = construirResumen();
        model.addAttribute("active", "operaciones");
        agregarResumen(model, r);
        model.addAttribute("servicios", serviciosRecientes(12));
        model.addAttribute("incidencias", incidenciasAbiertas(10));
        model.addAttribute("maquinariasCriticas", maquinariaRepository.findAll().stream()
                .filter(m -> !"OPERATIVO".equals(m.getEstadoOperativo()))
                .limit(10).toList());
        return "gerencia/operaciones";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        Resumen r = construirResumen();
        model.addAttribute("active", "reportes");
        agregarResumen(model, r);
        model.addAttribute("reportes", List.of(
                Map.of("titulo", "Resumen comercial", "descripcion", "Solicitudes, cotizaciones, contratos y ventas.", "url", "/ventas/reportes", "icono", "bi-briefcase"),
                Map.of("titulo", "Reporte de marketing", "descripcion", "Leads, oportunidades y campañas.", "url", "/marketing/reportes", "icono", "bi-megaphone"),
                Map.of("titulo", "Operaciones", "descripcion", "Servicios, asignaciones e incidencias.", "url", "/operaciones/dashboard", "icono", "bi-truck"),
                Map.of("titulo", "Logística y almacén", "descripcion", "Inventario, movimientos, compras y recepciones.", "url", "/logistica/dashboard", "icono", "bi-box-seam"),
                Map.of("titulo", "Recursos humanos", "descripcion", "Personal, documentación y vencimientos.", "url", "/rrhh/dashboard", "icono", "bi-people"),
                Map.of("titulo", "Mantenimiento", "descripcion", "Programación, costos y estado de mantenimientos.", "url", "/mantenimiento", "icono", "bi-tools")
        ));
        model.addAttribute("contratos", contratosRecientes(10));
        model.addAttribute("ordenes", ordenCompraRepository.findAll().stream().sorted(Comparator.comparing(OrdenCompra::getFechaEmision, Comparator.nullsLast(Comparator.reverseOrder()))).limit(10).toList());
        return "gerencia/reportes";
    }

    private void agregarResumen(Model model, Resumen r) {
        model.addAttribute("mes", LocalDate.now().getMonth().toString());
        model.addAttribute("totalClientes", r.totalClientes);
        model.addAttribute("empleadosActivos", r.empleadosActivos);
        model.addAttribute("maquinariaOperativa", r.maquinariaOperativa);
        model.addAttribute("totalMaquinaria", r.totalMaquinaria);
        model.addAttribute("serviciosPendientes", r.serviciosPendientes);
        model.addAttribute("serviciosEnCurso", r.serviciosEnCurso);
        model.addAttribute("incidenciasAbiertas", r.incidenciasAbiertas);
        model.addAttribute("stockCritico", r.stockCritico);
        model.addAttribute("ordenesPendientes", r.ordenesPendientes);
        model.addAttribute("documentosVencidos", r.documentosVencidos);
        model.addAttribute("documentosPorVencer", r.documentosPorVencer);
        model.addAttribute("ingresosTotales", r.ingresosTotales);
        model.addAttribute("costosTotales", r.costosTotales);
        model.addAttribute("resultadoOperativo", r.resultadoOperativo);
        model.addAttribute("margen", r.margen);
        model.addAttribute("ingresosMes", r.ingresosMes);
        model.addAttribute("costosMes", r.costosMes);
        model.addAttribute("resultadoMes", r.resultadoMes);
        model.addAttribute("oportunidades", r.oportunidades);
        model.addAttribute("pipeline", r.pipeline);
    }

    private Resumen construirResumen() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue(), anio = hoy.getYear();

        List<Contrato> contratos = contratoRepository.findAllByOrderByFechaInicioDesc();
        List<Venta> ventas = ventaRepository.findAll();
        List<OrdenCompra> ordenes = ordenCompraRepository.findAll();
        List<Mantenimiento> mantenimientos = mantenimientoRepository.findAll();
        List<Maquinaria> maquinarias = maquinariaRepository.findAll();
        List<Servicio> servicios = servicioRepository.findAll();
        List<Repuesto> repuestos = repuestoRepository.findAll();
        List<Empleado> empleados = empleadoRepository.findAll();
        long documentosVencidos = documentoRepository.findAll().stream().filter(d -> Boolean.TRUE.equals(d.getEstado()) && d.getFechaVencimiento() != null && d.getFechaVencimiento().isBefore(hoy)).count();
        long documentosPorVencer = documentoRepository.findAll().stream().filter(d -> Boolean.TRUE.equals(d.getEstado()) && d.getFechaVencimiento() != null && !d.getFechaVencimiento().isBefore(hoy) && !d.getFechaVencimiento().isAfter(hoy.plusDays(30))).count();

        BigDecimal ingresosVentas = ventas.stream().filter(v -> v.getFechaVenta() != null && !"CANCELADA".equalsIgnoreCase(v.getEstado())).map(Venta::getTotalFinal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ingresosServicios = contratos.stream().filter(c -> c.getTipoContrato() != null && !"VENTA".equalsIgnoreCase(c.getTipoContrato()) && !"CANCELADO".equalsIgnoreCase(c.getEstado())).map(Contrato::getMontoTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costosCompras = ordenes.stream().filter(o -> !"CANCELADA".equalsIgnoreCase(o.getEstado())).map(OrdenCompra::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costosMantenimiento = mantenimientos.stream().map(Mantenimiento::getCosto).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosMes = ventas.stream().filter(v -> esMes(v.getFechaVenta(), anio, mes) && !"CANCELADA".equalsIgnoreCase(v.getEstado())).map(Venta::getTotalFinal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        ingresosMes = ingresosMes.add(contratos.stream().filter(c -> c.getTipoContrato() != null && !"VENTA".equalsIgnoreCase(c.getTipoContrato()) && !"CANCELADO".equalsIgnoreCase(c.getEstado()) && esMes(c.getFechaInicio(), anio, mes)).map(Contrato::getMontoTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal costosComprasMes = ordenes.stream().filter(o -> esMes(o.getFechaEmision(), anio, mes) && !"CANCELADA".equalsIgnoreCase(o.getEstado())).map(OrdenCompra::getTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costosMantMes = mantenimientos.stream().filter(m -> esMes(fechaCosto(m), anio, mes)).map(Mantenimiento::getCosto).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costosMes = costosComprasMes.add(costosMantMes);

        BigDecimal ingresos = ingresosVentas.add(ingresosServicios);
        BigDecimal costos = costosCompras.add(costosMantenimiento);
        BigDecimal resultado = ingresos.subtract(costos);
        BigDecimal resultadoMes = ingresosMes.subtract(costosMes);
        BigDecimal margen = ingresos.signum() == 0 ? BigDecimal.ZERO : resultado.multiply(BigDecimal.valueOf(100)).divide(ingresos, 2, RoundingMode.HALF_UP);

        long oportunidades = oportunidadRepository.count();
        BigDecimal pipeline = Optional.ofNullable(oportunidadRepository.sumAllMontoEstimado()).orElse(BigDecimal.ZERO);

        Resumen r = new Resumen();
        r.totalClientes = clienteRepository.count();
        r.empleadosActivos = empleados.stream().filter(e -> Boolean.TRUE.equals(e.getEstado())).count();
        r.totalMaquinaria = maquinarias.size();
        r.maquinariaOperativa = maquinarias.stream().filter(m -> "OPERATIVO".equals(m.getEstadoOperativo())).count();
        r.serviciosPendientes = servicios.stream().filter(s -> "POR_ASIGNAR".equals(s.getEstado())).count();
        r.serviciosEnCurso = servicios.stream().filter(s -> "EN_CURSO".equals(s.getEstado())).count();
        r.incidenciasAbiertas = incidenciaRepository.findByEstado("ABIERTA").size();
        r.stockCritico = repuestos.stream().filter(x -> Boolean.TRUE.equals(x.getEstado()) && x.getStockActual() != null && x.getStockMinimo() != null && x.getStockActual() <= x.getStockMinimo()).count();
        r.ordenesPendientes = ordenes.stream().filter(o -> "PENDIENTE".equals(o.getEstado())).count();
        r.documentosVencidos = documentosVencidos;
        r.documentosPorVencer = documentosPorVencer;
        r.ingresosVentas = ingresosVentas;
        r.ingresosServiciosAlquiler = ingresosServicios;
        r.costosCompras = costosCompras;
        r.costosMantenimiento = costosMantenimiento;
        r.ingresosTotales = ingresos;
        r.costosTotales = costos;
        r.resultadoOperativo = resultado;
        r.margen = margen;
        r.ingresosMes = ingresosMes;
        r.costosMes = costosMes;
        r.resultadoMes = resultadoMes;
        r.oportunidades = oportunidades;
        r.pipeline = pipeline;
        return r;
    }

    private List<Contrato> contratosRecientes() { return contratosRecientes(8); }
    private List<Contrato> contratosRecientes(int n) { return contratoRepository.findAllByOrderByFechaInicioDesc().stream().limit(n).toList(); }

    private List<Servicio> serviciosRecientes() { return serviciosRecientes(8); }
    private List<Servicio> serviciosRecientes(int n) { return servicioRepository.findAll().stream().sorted(Comparator.comparing(Servicio::getFechaProgramada, Comparator.nullsLast(Comparator.reverseOrder()))).limit(n).toList(); }

    private List<Incidencia> incidenciasAbiertas(int n) { return incidenciaRepository.findAll().stream().filter(i -> i.getEstado() != null && !"CERRADA".equals(i.getEstado()) && !"RESUELTA".equals(i.getEstado())).sorted(Comparator.comparing(Incidencia::getFecha, Comparator.nullsLast(Comparator.reverseOrder()))).limit(n).toList(); }

    private List<Repuesto> repuestosCriticos() { return repuestoRepository.findAll().stream().filter(r -> Boolean.TRUE.equals(r.getEstado()) && r.getStockActual() != null && r.getStockMinimo() != null && r.getStockActual() <= r.getStockMinimo()).sorted(Comparator.comparing(Repuesto::getStockActual)).limit(12).toList(); }

    private List<Map<String, Object>> construirAlertas() {
        List<Map<String, Object>> alertas = new ArrayList<>();
        if (construirResumen().incidenciasAbiertas > 0) alertas.add(alerta("Incidencias operativas abiertas", construirResumen().incidenciasAbiertas + " incidencia(s) requieren seguimiento.", "danger", "/gerencia/operaciones"));
        if (construirResumen().stockCritico > 0) alertas.add(alerta("Stock crítico", construirResumen().stockCritico + " repuesto(s) están en mínimo o por debajo.", "warning", "/gerencia/indicadores"));
        if (construirResumen().documentosVencidos > 0) alertas.add(alerta("Documentación vencida", construirResumen().documentosVencidos + " documento(s) de personal están vencidos.", "danger", "/gerencia/reportes"));
        if (construirResumen().ordenesPendientes > 0) alertas.add(alerta("Órdenes de compra pendientes", construirResumen().ordenesPendientes + " orden(es) esperan atención.", "warning", "/gerencia/reportes"));
        if (construirResumen().serviciosPendientes > 0) alertas.add(alerta("Servicios por asignar", construirResumen().serviciosPendientes + " servicio(s) todavía no tienen asignación.", "info", "/gerencia/operaciones"));
        return alertas;
    }

    private Map<String, Object> alerta(String titulo, String texto, String tipo, String url) {
        Map<String, Object> m = new HashMap<>(); m.put("titulo", titulo); m.put("texto", texto); m.put("tipo", tipo); m.put("url", url); return m;
    }

    private List<Oportunidad> topOportunidades() { return oportunidadRepository.findAll().stream().sorted(Comparator.comparing(Oportunidad::getMontoEstimado, Comparator.nullsLast(Comparator.reverseOrder()))).limit(5).toList(); }

    private <T> Map<String, Long> agrupar(List<T> lista, Function<T, String> fn) {
        return lista.stream().collect(Collectors.groupingBy(x -> Optional.ofNullable(fn.apply(x)).orElse("SIN DATO"), TreeMap::new, Collectors.counting()));
    }

    private boolean esMes(LocalDate fecha, int anio, int mes) { return fecha != null && fecha.getYear() == anio && fecha.getMonthValue() == mes; }
    private LocalDate fechaCosto(Mantenimiento m) { return m.getFechaFin() != null ? m.getFechaFin() : (m.getFechaInicio() != null ? m.getFechaInicio() : m.getFechaProgramada()); }

    private static class Resumen {
        long totalClientes, empleadosActivos, totalMaquinaria, maquinariaOperativa, serviciosPendientes, serviciosEnCurso, incidenciasAbiertas, stockCritico, ordenesPendientes, documentosVencidos, documentosPorVencer, oportunidades;
        BigDecimal ingresosVentas = BigDecimal.ZERO, ingresosServiciosAlquiler = BigDecimal.ZERO, costosCompras = BigDecimal.ZERO, costosMantenimiento = BigDecimal.ZERO, ingresosTotales = BigDecimal.ZERO, costosTotales = BigDecimal.ZERO, resultadoOperativo = BigDecimal.ZERO, margen = BigDecimal.ZERO, ingresosMes = BigDecimal.ZERO, costosMes = BigDecimal.ZERO, resultadoMes = BigDecimal.ZERO, pipeline = BigDecimal.ZERO;
    }
}
