package com.transmaqsur.sigem.api;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.UbicacionGps;
import com.transmaqsur.sigem.service.GpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API para dispositivos GPS o la app del conductor.
 * <pre>
 * POST /api/gps
 * X-API-KEY: transmaq-gps-2026
 * {"codigoUnidad":"VOL-001","latitud":-16.39,"longitud":-71.54,"velocidad":45,"referencia":"Variante de Uchumayo"}
 * </pre>
 */
@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
public class GpsApiController {

    public record PosicionRequest(String codigoUnidad, Double latitud, Double longitud, Double velocidad, String referencia) {
    }

    public record PosicionResponse(String unidad, double latitud, double longitud, Double velocidad, String fechaHora, String estado) {
        static PosicionResponse de(UbicacionGps g) {
            return new PosicionResponse(g.getUnidad().getCodigo(), g.getLatitud(), g.getLongitud(), g.getVelocidad(),
                    g.getFechaHora().toString(), g.getUnidad().getEstado().getLabel());
        }
    }

    private final GpsService gpsService;

    @Value("${sigem.gps.api-key}")
    private String apiKey;

    @PostMapping
    public ResponseEntity<?> registrar(@RequestHeader(value = "X-API-KEY", required = false) String key,
                                       @RequestBody PosicionRequest req) {
        if (!apiKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Clave de dispositivo inválida"));
        }
        if (req.codigoUnidad() == null || req.latitud() == null || req.longitud() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "codigoUnidad, latitud y longitud son obligatorios"));
        }
        UbicacionGps g = gpsService.registrarDesdeDispositivo(req.codigoUnidad(), req.latitud(), req.longitud(),
                req.velocidad(), req.referencia());
        return ResponseEntity.status(HttpStatus.CREATED).body(PosicionResponse.de(g));
    }

    @GetMapping("/ultimas")
    public ResponseEntity<?> ultimas(@RequestHeader(value = "X-API-KEY", required = false) String key) {
        if (!apiKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Clave de dispositivo inválida"));
        }
        List<PosicionResponse> lista = gpsService.ultimasPosiciones().stream().map(PosicionResponse::de).toList();
        return ResponseEntity.ok(lista);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, String>> negocio(NegocioException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
