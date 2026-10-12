package com.transur.sigem.config;

import com.transur.sigem.mantenimiento.entity.PlanMantenimiento;
import com.transur.sigem.mantenimiento.repository.PlanMantenimientoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.entity.Asignacion;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.entity.UbicacionGps;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.operaciones.repository.UbicacionGpsRepository;
import com.transur.sigem.rrhh.entity.DocumentoEmpleado;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos demo de las funciones operativas añadidas: lecturas de horómetro/km, vencimientos
 * de SOAT y revisión técnica, planes preventivos, cierre de servicios, documentos de
 * conductores y posiciones GPS. Idempotente: cada bloque solo se carga si está vacío.
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class DemoOperacionesSeeder implements CommandLineRunner {

    private final MaquinariaRepository maquinariaRepo;
    private final PlanMantenimientoRepository planRepo;
    private final ServicioRepository servicioRepo;
    private final AsignacionRepository asignacionRepo;
    private final UbicacionGpsRepository gpsRepo;
    private final EmpleadoRepository empleadoRepo;
    private final DocumentoEmpleadoRepository documentoRepo;

    @Override
    @Transactional
    public void run(String... args) {
        List<Maquinaria> maquinas = maquinariaRepo.findAll();
        if (maquinas.isEmpty()) return;
        lecturasYDocumentos(maquinas);
        planes(maquinas);
        cierreServicios();
        documentosConductores();
        gps(maquinas);
    }

    private void lecturasYDocumentos(List<Maquinaria> maquinas) {
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < maquinas.size(); i++) {
            Maquinaria m = maquinas.get(i);
            if (m.getHorometroActual() == null || m.getHorometroActual().signum() == 0) {
                m.setHorometroActual(BigDecimal.valueOf(1200 + i * 137L));
            }
            if (m.getKilometrajeActual() == null || m.getKilometrajeActual().signum() == 0) {
                m.setKilometrajeActual(BigDecimal.valueOf(35000 + i * 2650L));
            }
            if (m.getSoatVencimiento() == null) m.setSoatVencimiento(hoy.plusDays(i == 1 ? 12 : 90 + i * 20L));
            if (m.getRevisionTecnicaVencimiento() == null) m.setRevisionTecnicaVencimiento(hoy.plusDays(i == 2 ? -3 : 120 + i * 15L));
        }
    }

    private void planes(List<Maquinaria> maquinas) {
        if (planRepo.count() > 0) return;
        // Distintos avances para mostrar las tres situaciones: OK, PRÓXIMO (>= 90 %) y VENCIDO.
        int[] avanceHoras = {120, 232, 262, 60, 180, 240};
        for (int i = 0; i < Math.min(6, maquinas.size()); i++) {
            Maquinaria m = maquinas.get(i);
            plan(m, "Cambio de aceite y filtros de motor", PlanMantenimiento.HOROMETRO, 250,
                    m.getHorometroActual().subtract(BigDecimal.valueOf(avanceHoras[i])), null);
            if (i % 2 == 0) {
                plan(m, "Revisión de frenos y neumáticos", PlanMantenimiento.KILOMETRAJE, 10000,
                        m.getKilometrajeActual().subtract(BigDecimal.valueOf(i == 2 ? 9300 : 4000)), null);
            }
            if (i % 3 == 0) {
                plan(m, "Inspección general de seguridad", PlanMantenimiento.FECHA, 180, null,
                        LocalDate.now().minusDays(i == 0 ? 170 : 40));
            }
        }
    }

    private void plan(Maquinaria m, String actividad, String criterio, int frecuencia, BigDecimal ultimoValor, LocalDate ultimaFecha) {
        PlanMantenimiento p = new PlanMantenimiento();
        p.setMaquinaria(m);
        p.setActividad(actividad);
        p.setCriterio(criterio);
        p.setFrecuencia(BigDecimal.valueOf(frecuencia));
        p.setUltimoValor(ultimoValor != null ? ultimoValor.max(BigDecimal.ZERO) : BigDecimal.ZERO);
        p.setUltimaFecha(ultimaFecha);
        p.setActivo(true);
        planRepo.save(p);
    }

    private void cierreServicios() {
        int i = 0;
        for (Servicio s : servicioRepo.findAll()) {
            i++;
            if (s.getFechaFinProgramada() == null) s.setFechaFinProgramada(s.getFechaProgramada().plusHours(10));
            if ("EN_CURSO".equals(s.getEstado()) && s.getFechaInicioReal() == null) {
                s.setFechaInicioReal(s.getFechaProgramada().plusMinutes(20));
            }
            if (!"FINALIZADO".equals(s.getEstado()) || s.getHorasUso() != null) continue;
            BigDecimal horas = BigDecimal.valueOf(6 + i % 5);
            BigDecimal km = BigDecimal.valueOf(40 + (i * 23) % 160);
            s.setFechaInicioReal(s.getFechaProgramada().plusMinutes(15));
            s.setFechaFinReal(s.getFechaInicioReal().plusHours(horas.longValue()));
            s.setHorasUso(horas);
            s.setKmRecorridos(km);
            asignacionRepo.findByServicioId(s.getId()).stream().findFirst().map(Asignacion::getMaquinaria).ifPresent(m -> {
                s.setHorometroFinal(m.getHorometroActual());
                s.setHorometroInicial(m.getHorometroActual().subtract(horas));
                s.setKmFinal(m.getKilometrajeActual());
                s.setKmInicial(m.getKilometrajeActual().subtract(km));
            });
        }
    }

    private void documentosConductores() {
        if (documentoRepo.count() > 0) return;
        int i = 0;
        for (Empleado e : empleadoRepo.findAll()) {
            if (e.getCargo() == null || !(e.getCargo().toUpperCase().contains("CONDUCTOR") || e.getCargo().toUpperCase().contains("OPERADOR"))) continue;
            DocumentoEmpleado d = new DocumentoEmpleado();
            d.setEmpleado(e);
            d.setTipoDocumento("LICENCIA_A-IIIc");
            d.setNumeroDocumento("Q" + (40000000 + e.getId()));
            d.setFechaEmision(LocalDate.now().minusYears(2));
            d.setFechaVencimiento(LocalDate.now().plusDays(i == 0 ? 18 : 200 + i * 30L));
            d.setEstado(true);
            documentoRepo.save(d);
            if (++i >= 6) break;
        }
    }

    private void gps(List<Maquinaria> maquinas) {
        if (gpsRepo.count() > 0) return;
        // Arequipa: Parque Industrial, Cerro Verde, Yura, Characato y Cayma.
        double[][] base = {{-16.4205, -71.5203}, {-16.5300, -71.5900}, {-16.2500, -71.6800}, {-16.4700, -71.4800}, {-16.3600, -71.5500}};
        LocalDateTime inicio = LocalDateTime.now().minusHours(3);
        for (int u = 0; u < Math.min(5, maquinas.size()); u++) {
            Maquinaria m = maquinas.get(u);
            Servicio servicio = asignacionRepo.findByMaquinariaId(m.getId()).stream()
                    .filter(a -> "ACTIVA".equals(a.getEstado())).map(Asignacion::getServicio).findFirst().orElse(null);
            for (int p = 0; p < 6; p++) {
                UbicacionGps g = new UbicacionGps();
                g.setMaquinaria(m);
                g.setServicio(servicio);
                g.setLatitud(Math.round((base[u][0] + p * 0.0042) * 1e6) / 1e6);
                g.setLongitud(Math.round((base[u][1] + p * 0.0031 * (u % 2 == 0 ? 1 : -1)) * 1e6) / 1e6);
                g.setVelocidad(p == 5 ? 0.0 : 25.0 + p * 4);
                g.setReferencia(p == 0 ? "Salida de base" : p == 5 ? "Llegada a obra" : "En ruta");
                g.setFechaHora(inicio.plusMinutes(p * 30L));
                g.setRegistradoPor("dispositivo");
                gpsRepo.save(g);
            }
        }
    }
}
