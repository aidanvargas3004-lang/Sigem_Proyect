package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.UbicacionGps;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.FuenteGps;
import com.transmaqsur.sigem.repository.UbicacionGpsRepository;
import com.transmaqsur.sigem.repository.UnidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Registro y consulta de la ubicación GPS de las unidades. */
@Service
@RequiredArgsConstructor
@Transactional
public class GpsService {

    /** Base de operaciones en Arequipa (Parque Industrial). */
    public static final double BASE_LAT = -16.4180;
    public static final double BASE_LON = -71.5200;

    private final UbicacionGpsRepository gpsRepository;
    private final UnidadRepository unidadRepository;
    private final UnidadService unidadService;

    public UbicacionGps registrar(UbicacionGps form) {
        form.setUnidad(unidadService.obtener(form.getUnidad().getId()));
        if (form.getFechaHora() == null) {
            form.setFechaHora(LocalDateTime.now());
        }
        if (form.getFechaHora().isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new NegocioException("La fecha y hora de la posición no puede ser futura");
        }
        return gpsRepository.save(form);
    }

    /** Punto de entrada para dispositivos GPS o la app del conductor. */
    public UbicacionGps registrarDesdeDispositivo(String codigoUnidad, double lat, double lon, Double velocidad, String referencia) {
        Unidad u = unidadRepository.findByCodigo(codigoUnidad.trim().toUpperCase())
                .orElseThrow(() -> new NegocioException("No existe la unidad " + codigoUnidad));
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new NegocioException("Coordenadas inválidas");
        }
        UbicacionGps g = new UbicacionGps();
        g.setUnidad(u);
        g.setLatitud(lat);
        g.setLongitud(lon);
        g.setVelocidad(velocidad);
        g.setReferencia(referencia);
        g.setFuente(FuenteGps.API);
        return gpsRepository.save(g);
    }

    @Transactional(readOnly = true)
    public List<UbicacionGps> ultimasPosiciones() {
        return gpsRepository.ultimasPosiciones();
    }

    @Transactional(readOnly = true)
    public List<UbicacionGps> recorrido(Long unidadId, LocalDate fecha) {
        return gpsRepository.findByUnidadIdAndFechaHoraBetweenOrderByFechaHoraAsc(unidadId, fecha.atStartOfDay(),
                fecha.plusDays(1).atStartOfDay());
    }

    @Transactional(readOnly = true)
    public Page<UbicacionGps> historial(Pageable pageable) {
        return gpsRepository.findAllByOrderByFechaHoraDesc(pageable);
    }

    /** Simula un reporte GPS de cada unidad en servicio (útil para demostraciones sin dispositivos). */
    public int simular() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int n = 0;
        for (Unidad u : unidadRepository.findAllByOrderByCodigoAsc()) {
            if (u.getEstado() != EstadoUnidad.EN_SERVICIO) {
                continue;
            }
            UbicacionGps ultima = gpsRepository.findFirstByUnidadIdOrderByFechaHoraDesc(u.getId()).orElse(null);
            double lat = ultima != null ? ultima.getLatitud() : BASE_LAT;
            double lon = ultima != null ? ultima.getLongitud() : BASE_LON;
            UbicacionGps g = new UbicacionGps();
            g.setUnidad(u);
            g.setLatitud(lat + rnd.nextDouble(-0.008, 0.008));
            g.setLongitud(lon + rnd.nextDouble(-0.008, 0.008));
            g.setVelocidad(u.getTipo().isVehiculo() ? Math.round(rnd.nextDouble(15, 70)) * 1.0 : Math.round(rnd.nextDouble(0, 8)) * 1.0);
            g.setFuente(FuenteGps.SIMULADOR);
            g.setReferencia("Posición simulada");
            gpsRepository.save(g);
            n++;
        }
        return n;
    }
}
