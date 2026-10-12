package com.transur.sigem.operaciones.controller;

import com.transur.sigem.operaciones.dto.UbicacionGpsRequest;
import com.transur.sigem.operaciones.dto.UbicacionGpsResponse;
import com.transur.sigem.operaciones.service.UbicacionGpsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** API para que el dispositivo o la app del operador envíe posiciones (JWT). */
@RestController
@RequestMapping("/api/gps")
@RequiredArgsConstructor
public class GpsApiController {

    private final UbicacionGpsService gpsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UbicacionGpsResponse registrar(@Valid @RequestBody UbicacionGpsRequest request) {
        return UbicacionGpsResponse.de(gpsService.registrar(request));
    }

    @GetMapping("/ultimas")
    public List<UbicacionGpsResponse> ultimas() {
        return gpsService.ultimasPosiciones().stream().map(UbicacionGpsResponse::de).toList();
    }

    @GetMapping("/maquinaria/{id}")
    public List<UbicacionGpsResponse> recorrido(@PathVariable Long id) {
        return gpsService.recorrido(id).stream().map(UbicacionGpsResponse::de).toList();
    }
}
