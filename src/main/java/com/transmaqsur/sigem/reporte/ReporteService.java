package com.transmaqsur.sigem.reporte;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Inventario;
import com.transmaqsur.sigem.model.Operacion;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.RepuestoUtilizado;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.Venta;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.EstadoOrden;
import com.transmaqsur.sigem.model.enums.EstadoVenta;
import com.transmaqsur.sigem.repository.AlquilerRepository;
import com.transmaqsur.sigem.repository.InventarioRepository;
import com.transmaqsur.sigem.repository.OrdenMantenimientoRepository;
import com.transmaqsur.sigem.repository.RepuestoUtilizadoRepository;
import com.transmaqsur.sigem.repository.ServicioTransporteRepository;
import com.transmaqsur.sigem.repository.UnidadRepository;
import com.transmaqsur.sigem.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/** Reportes de gestión para la toma de decisiones. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Catálogo de reportes: clave → [título, descripción]. */
    public static final Map<String, String[]> CATALOGO = new LinkedHashMap<>();

    static {
        CATALOGO.put("flota", new String[]{"Utilización y rentabilidad de la flota",
                "Ingresos generados y costo de mantenimiento por unidad en el periodo."});
        CATALOGO.put("operaciones", new String[]{"Horas y kilómetros facturables",
                "Alquileres y servicios finalizados con cantidad facturable, tarifa y monto."});
        CATALOGO.put("mantenimiento", new String[]{"Mantenimientos y costos",
                "Órdenes preventivas y correctivas con costo de repuestos, mano de obra y terceros."});
        CATALOGO.put("repuestos", new String[]{"Consumo de repuestos",
                "Repuestos utilizados en las órdenes de mantenimiento."});
        CATALOGO.put("ventas", new String[]{"Registro de ventas",
                "Comprobantes emitidos en el periodo con subtotal, IGV y total."});
        CATALOGO.put("clientes", new String[]{"Facturación por cliente",
                "Total facturado y número de comprobantes por cliente."});
        CATALOGO.put("inventario", new String[]{"Inventario valorizado",
                "Stock actual por almacén, valorizado al último costo (no depende del periodo)."});
    }

    private final UnidadRepository unidadRepository;
    private final AlquilerRepository alquilerRepository;
    private final ServicioTransporteRepository servicioRepository;
    private final OrdenMantenimientoRepository ordenRepository;
    private final RepuestoUtilizadoRepository repuestoUtilizadoRepository;
    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;

    public Reporte generar(String clave, LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new NegocioException("La fecha inicial no puede ser posterior a la final");
        }
        return switch (clave) {
            case "flota" -> flota(desde, hasta);
            case "operaciones" -> operaciones(desde, hasta);
            case "mantenimiento" -> mantenimiento(desde, hasta);
            case "repuestos" -> repuestos(desde, hasta);
            case "ventas" -> ventas(desde, hasta);
            case "clientes" -> clientes(desde, hasta);
            case "inventario" -> inventario();
            default -> throw new NegocioException("Reporte desconocido: " + clave);
        };
    }

    private Reporte flota(LocalDate desde, LocalDate hasta) {
        List<Operacion> ops = operacionesFinalizadas(desde, hasta);
        List<OrdenMantenimiento> ots = ordenRepository.findAllByOrderByIdDesc().stream()
                .filter(o -> o.getEstado() == EstadoOrden.COMPLETADA && enRango(o.getFechaFin(), desde, hasta)).toList();
        List<List<Object>> filas = new ArrayList<>();
        BigDecimal tIng = BigDecimal.ZERO;
        BigDecimal tCosto = BigDecimal.ZERO;
        for (Unidad u : unidadRepository.findAllByOrderByCodigoAsc()) {
            List<Operacion> propias = ops.stream().filter(o -> o.getUnidad().getId().equals(u.getId())).toList();
            BigDecimal ingresos = suma(propias, Operacion::getMontoTotal);
            BigDecimal costo = suma(ots.stream().filter(o -> o.getUnidad().getId().equals(u.getId())).toList(),
                    OrdenMantenimiento::getCostoTotal);
            tIng = tIng.add(ingresos);
            tCosto = tCosto.add(costo);
            filas.add(Arrays.asList(u.getCodigo(), u.getTipo().getLabel(), u.getMarca() + " " + u.getModelo(), u.getEstado().getLabel(),
                    u.getLecturaActual().stripTrailingZeros().toPlainString() + " " + u.getTipo().getMedida(),
                    u.getPorcentajeUso() + "%", propias.size(), ingresos, costo, ingresos.subtract(costo)));
        }
        return new Reporte("flota", CATALOGO.get("flota")[0], periodo(desde, hasta),
                List.of("Código", "Tipo", "Marca / modelo", "Estado", "Lectura", "Uso intervalo mant.", "Operaciones",
                        "Ingresos S/", "Costo mant. S/", "Margen S/"),
                filas, Arrays.asList("TOTAL", null, null, null, null, null, ops.size(), tIng, tCosto, tIng.subtract(tCosto)));
    }

    private Reporte operaciones(LocalDate desde, LocalDate hasta) {
        List<Operacion> ops = operacionesFinalizadas(desde, hasta);
        List<List<Object>> filas = new ArrayList<>();
        for (Operacion o : ops) {
            filas.add(Arrays.asList(o.getCodigo(), o.getCodigo().startsWith("ALQ") ? "Alquiler" : "Transporte",
                    o.getCliente().getRazonSocial(), o.getUnidad().getCodigo(),
                    o.getEmpleado() != null ? o.getEmpleado().getNombreCompleto() : "-",
                    FH.format(o.getInicioReal()), FH.format(o.getFinReal()), o.getModalidad().getLabel(),
                    o.getCantidadFacturable(), o.getModalidad().getUnidad(), o.getTarifa(), o.getMontoTotal(),
                    o.isFacturado() ? "Sí" : "No"));
        }
        return new Reporte("operaciones", CATALOGO.get("operaciones")[0], periodo(desde, hasta),
                List.of("Código", "Tipo", "Cliente", "Unidad", "Operador", "Inicio", "Fin", "Modalidad", "Cantidad", "Und.",
                        "Tarifa S/", "Monto S/", "Facturado"),
                filas, Arrays.asList("TOTAL", null, null, null, null, null, null, null, null, null, null,
                        suma(ops, Operacion::getMontoTotal), null));
    }

    private Reporte mantenimiento(LocalDate desde, LocalDate hasta) {
        List<OrdenMantenimiento> ots = ordenRepository.findAllByOrderByIdDesc().stream()
                .filter(o -> !o.getFechaProgramada().isBefore(desde) && !o.getFechaProgramada().isAfter(hasta))
                .sorted(Comparator.comparing(OrdenMantenimiento::getFechaProgramada)).toList();
        List<List<Object>> filas = new ArrayList<>();
        for (OrdenMantenimiento o : ots) {
            filas.add(Arrays.asList(o.getCodigo(), o.getUnidad().getCodigo(), o.getTipo().getLabel(), o.getPrioridad().getLabel(),
                    o.getEstado().getLabel(), F.format(o.getFechaProgramada()),
                    o.getTecnico() != null ? o.getTecnico().getNombreCompleto() : "-",
                    o.getCostoRepuestos(), o.getCostoManoObra(), o.getCostoServiciosTerceros(), o.getCostoTotal()));
        }
        return new Reporte("mantenimiento", CATALOGO.get("mantenimiento")[0], periodo(desde, hasta),
                List.of("OT", "Unidad", "Tipo", "Prioridad", "Estado", "Programada", "Técnico", "Repuestos S/", "Mano de obra S/",
                        "Terceros S/", "Total S/"),
                filas, Arrays.asList("TOTAL", null, null, null, null, null, null,
                        suma(ots, OrdenMantenimiento::getCostoRepuestos), suma(ots, OrdenMantenimiento::getCostoManoObra),
                        suma(ots, OrdenMantenimiento::getCostoServiciosTerceros), suma(ots, OrdenMantenimiento::getCostoTotal)));
    }

    private Reporte repuestos(LocalDate desde, LocalDate hasta) {
        List<RepuestoUtilizado> lista = repuestoUtilizadoRepository.findAllByOrderByIdDesc().stream()
                .filter(r -> enRango(r.getFechaCreacion(), desde, hasta)).toList();
        List<List<Object>> filas = new ArrayList<>();
        for (RepuestoUtilizado r : lista) {
            filas.add(Arrays.asList(FH.format(r.getFechaCreacion()), r.getOrden().getCodigo(), r.getOrden().getUnidad().getCodigo(),
                    r.getRepuesto().getDescripcionCompleta(), r.getAlmacen().getNombre(), r.getCantidad(),
                    r.getRepuesto().getUnidadMedida(), r.getPrecioUnitario(), r.getSubtotal()));
        }
        return new Reporte("repuestos", CATALOGO.get("repuestos")[0], periodo(desde, hasta),
                List.of("Fecha", "OT", "Unidad", "Repuesto", "Almacén", "Cantidad", "Und.", "P. unit. S/", "Subtotal S/"),
                filas, Arrays.asList("TOTAL", null, null, null, null, null, null, null, suma(lista, RepuestoUtilizado::getSubtotal)));
    }

    private Reporte ventas(LocalDate desde, LocalDate hasta) {
        List<Venta> ventas = ventasEmitidas(desde, hasta);
        List<List<Object>> filas = new ArrayList<>();
        for (Venta v : ventas) {
            filas.add(Arrays.asList(v.getNumeroComprobante(), v.getTipoComprobante().getLabel(), F.format(v.getFechaEmision()),
                    v.getCliente().getNumeroDocumento(), v.getCliente().getRazonSocial(), v.getEstado().getLabel(),
                    v.getSubtotal(), v.getIgv(), v.getTotal()));
        }
        return new Reporte("ventas", CATALOGO.get("ventas")[0], periodo(desde, hasta),
                List.of("Comprobante", "Tipo", "Fecha", "RUC/DNI", "Cliente", "Estado", "Subtotal S/", "IGV S/", "Total S/"),
                filas, Arrays.asList("TOTAL", null, null, null, null, null,
                        suma(ventas, Venta::getSubtotal), suma(ventas, Venta::getIgv), suma(ventas, Venta::getTotal)));
    }

    private Reporte clientes(LocalDate desde, LocalDate hasta) {
        Map<String, List<Venta>> porCliente = new LinkedHashMap<>();
        ventasEmitidas(desde, hasta).stream()
                .sorted(Comparator.comparing(v -> v.getCliente().getRazonSocial()))
                .forEach(v -> porCliente.computeIfAbsent(v.getCliente().getRazonSocial(), k -> new ArrayList<>()).add(v));
        List<List<Object>> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, List<Venta>> e : porCliente.entrySet()) {
            BigDecimal t = suma(e.getValue(), Venta::getTotal);
            BigDecimal pagado = suma(e.getValue().stream().filter(v -> v.getEstado() == EstadoVenta.PAGADA).toList(), Venta::getTotal);
            total = total.add(t);
            filas.add(Arrays.asList(e.getKey(), e.getValue().get(0).getCliente().getTipoCliente().getLabel(), e.getValue().size(),
                    t, pagado, t.subtract(pagado)));
        }
        filas.sort(Comparator.comparing((List<Object> f) -> (BigDecimal) f.get(3)).reversed());
        return new Reporte("clientes", CATALOGO.get("clientes")[0], periodo(desde, hasta),
                List.of("Cliente", "Tipo", "Comprobantes", "Facturado S/", "Cobrado S/", "Por cobrar S/"),
                filas, Arrays.asList("TOTAL", null, null, total, null, null));
    }

    private Reporte inventario() {
        List<Inventario> inv = inventarioRepository.findAllByOrderByAlmacenNombreAscRepuestoNombreAsc();
        List<List<Object>> filas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Inventario i : inv) {
            BigDecimal valor = i.getCantidad().multiply(i.getRepuesto().getPrecioUnitario());
            total = total.add(valor);
            filas.add(Arrays.asList(i.getAlmacen().getNombre(), i.getRepuesto().getCodigo(), i.getRepuesto().getNombre(),
                    i.getCantidad(), i.getRepuesto().getStockMinimo(), i.getRepuesto().getUnidadMedida(),
                    i.getRepuesto().getPrecioUnitario(), valor, i.isBajoMinimo() ? "BAJO MÍNIMO" : "OK"));
        }
        return new Reporte("inventario", CATALOGO.get("inventario")[0], "Al " + F.format(LocalDate.now()),
                List.of("Almacén", "Código", "Repuesto", "Stock", "Mínimo", "Und.", "Costo S/", "Valor S/", "Situación"),
                filas, Arrays.asList("TOTAL", null, null, null, null, null, null, total, null));
    }

    // ------------------------------------------------------------ utilidades

    private List<Operacion> operacionesFinalizadas(LocalDate desde, LocalDate hasta) {
        return Stream.concat(alquilerRepository.findAllByOrderByIdDesc().stream(), servicioRepository.findAllByOrderByIdDesc().stream())
                .map(o -> (Operacion) o)
                .filter(o -> o.getEstado() == EstadoOperacion.FINALIZADO && enRango(o.getFinReal(), desde, hasta))
                .sorted(Comparator.comparing(Operacion::getFinReal)).toList();
    }

    private List<Venta> ventasEmitidas(LocalDate desde, LocalDate hasta) {
        return ventaRepository.findByEstadoInAndFechaEmisionBetweenOrderByFechaEmisionAsc(
                List.of(EstadoVenta.EMITIDA, EstadoVenta.PAGADA), desde, hasta);
    }

    private static boolean enRango(LocalDateTime f, LocalDate desde, LocalDate hasta) {
        return f != null && !f.toLocalDate().isBefore(desde) && !f.toLocalDate().isAfter(hasta);
    }

    private static <T> BigDecimal suma(List<T> lista, Function<T, BigDecimal> campo) {
        return lista.stream().map(campo).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String periodo(LocalDate desde, LocalDate hasta) {
        return "Periodo: " + F.format(desde) + " al " + F.format(hasta);
    }
}
