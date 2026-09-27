package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.model.Inventario;
import com.transmaqsur.sigem.model.Operacion;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.EstadoSolicitud;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.EstadoVenta;
import com.transmaqsur.sigem.repository.AlquilerRepository;
import com.transmaqsur.sigem.repository.AsignacionRepository;
import com.transmaqsur.sigem.repository.ContratoRepository;
import com.transmaqsur.sigem.repository.InventarioRepository;
import com.transmaqsur.sigem.repository.OrdenMantenimientoRepository;
import com.transmaqsur.sigem.repository.ServicioTransporteRepository;
import com.transmaqsur.sigem.repository.SolicitudRepository;
import com.transmaqsur.sigem.repository.UnidadRepository;
import com.transmaqsur.sigem.repository.VentaRepository;
import com.transmaqsur.sigem.model.enums.EstadoContrato;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/** Indicadores consolidados para el panel principal. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<EstadoVenta> VENTAS_VALIDAS = List.of(EstadoVenta.EMITIDA, EstadoVenta.PAGADA);

    private final UnidadRepository unidadRepository;
    private final AlquilerRepository alquilerRepository;
    private final ServicioTransporteRepository servicioRepository;
    private final OrdenMantenimientoRepository ordenRepository;
    private final InventarioRepository inventarioRepository;
    private final ContratoRepository contratoRepository;
    private final VentaRepository ventaRepository;
    private final SolicitudRepository solicitudRepository;
    private final AsignacionRepository asignacionRepository;

    public record Resumen(
            Map<EstadoUnidad, Long> unidadesPorEstado,
            long totalUnidades,
            long operacionesEnCurso,
            long operacionesProgramadas,
            long ordenesAbiertas,
            long solicitudesPendientes,
            BigDecimal ventasMes,
            BigDecimal montoPorFacturar,
            long operacionesPorFacturar,
            List<Unidad> mantenimientoProximo,
            List<Inventario> stockBajo,
            List<Contrato> contratosPorVencer,
            List<Asignacion> proximasAsignaciones,
            List<String> mesesLabels,
            List<BigDecimal> ventasPorMes,
            int disponibilidadPorcentaje) {
    }

    public Resumen resumen() {
        Map<EstadoUnidad, Long> porEstado = new LinkedHashMap<>();
        long total = 0;
        for (EstadoUnidad e : EstadoUnidad.values()) {
            long n = unidadRepository.countByEstado(e);
            porEstado.put(e, n);
            total += n;
        }
        long operativas = total - porEstado.get(EstadoUnidad.FUERA_SERVICIO);
        int disponibilidad = operativas == 0 ? 0
                : (int) Math.round(100.0 * (operativas - porEstado.get(EstadoUnidad.EN_MANTENIMIENTO)) / operativas);

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        BigDecimal ventasMes = ventaRepository.totalEntre(inicioMes, hoy, VENTAS_VALIDAS);

        List<Operacion> porFacturar = Stream.concat(
                alquilerRepository.findByEstadoAndFacturadoFalseOrderByIdAsc(EstadoOperacion.FINALIZADO).stream(),
                servicioRepository.findByEstadoAndFacturadoFalseOrderByIdAsc(EstadoOperacion.FINALIZADO).stream()
        ).map(o -> (Operacion) o).toList();
        BigDecimal montoPorFacturar = porFacturar.stream().map(Operacion::getMontoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> meses = new ArrayList<>();
        List<BigDecimal> ventas = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate ini = inicioMes.minusMonths(i);
            LocalDate fin = ini.plusMonths(1).minusDays(1);
            String nombre = ini.getMonth().getDisplayName(TextStyle.SHORT, Locale.of("es", "PE"));
            meses.add(nombre.substring(0, 1).toUpperCase() + nombre.substring(1).replace(".", "") + " " + ini.getYear());
            ventas.add(ventaRepository.totalEntre(ini, fin, VENTAS_VALIDAS));
        }

        List<Unidad> mantenimiento = unidadRepository.findByEstadoNotOrderByCodigoAsc(EstadoUnidad.FUERA_SERVICIO).stream()
                .filter(Unidad::isRequiereMantenimiento).toList();
        List<Contrato> porVencer = contratoRepository.findByEstadoOrderByFechaFinAsc(EstadoContrato.VIGENTE).stream()
                .filter(Contrato::isPorVencer).toList();
        List<Asignacion> proximas = asignacionRepository.findByEstadoInOrderByFechaInicioAsc(EstadosActivos.ASIGNACION)
                .stream().limit(8).toList();

        return new Resumen(porEstado, total,
                alquilerRepository.countByEstado(EstadoOperacion.EN_CURSO) + servicioRepository.countByEstado(EstadoOperacion.EN_CURSO),
                alquilerRepository.countByEstado(EstadoOperacion.PROGRAMADO) + servicioRepository.countByEstado(EstadoOperacion.PROGRAMADO),
                ordenRepository.countByEstadoIn(EstadosActivos.ORDEN),
                solicitudRepository.countByEstado(EstadoSolicitud.REGISTRADA),
                ventasMes, montoPorFacturar, porFacturar.size(),
                mantenimiento, inventarioRepository.findBajoMinimo(), porVencer, proximas, meses, ventas, disponibilidad);
    }
}
