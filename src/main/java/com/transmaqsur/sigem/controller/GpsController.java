package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.model.UbicacionGps;
import com.transmaqsur.sigem.service.GpsService;
import com.transmaqsur.sigem.service.UnidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Mapa de la flota, registro de posiciones y recorridos. */
@Controller
@RequestMapping("/gps")
@RequiredArgsConstructor
public class GpsController {

    private final GpsService gpsService;
    private final UnidadService unidadService;

    @GetMapping
    public String mapa(Model model) {
        List<UbicacionGps> posiciones = gpsService.ultimasPosiciones();
        model.addAttribute("posiciones", posiciones);
        model.addAttribute("marcadores", posiciones.stream().map(p -> Map.of(
                "id", p.getUnidad().getId(), "codigo", p.getUnidad().getCodigo(), "desc", p.getUnidad().getDescripcion(),
                "estado", p.getUnidad().getEstado().getLabel(), "lat", p.getLatitud(), "lon", p.getLongitud(),
                "vel", p.getVelocidad() != null ? p.getVelocidad() : 0)).toList());
        model.addAttribute("unidades", unidadService.listarOperativas());
        UbicacionGps nueva = new UbicacionGps();
        nueva.setFechaHora(LocalDateTime.now().withSecond(0).withNano(0));
        model.addAttribute("ubicacion", nueva);
        model.addAttribute("baseLat", GpsService.BASE_LAT);
        model.addAttribute("baseLon", GpsService.BASE_LON);
        return "gps/mapa";
    }

    @GetMapping("/recorrido")
    public String recorrido(@RequestParam Long unidadId, @RequestParam(required = false) LocalDate fecha, Model model) {
        LocalDate f = fecha != null ? fecha : LocalDate.now();
        model.addAttribute("unidad", unidadService.obtener(unidadId));
        model.addAttribute("unidades", unidadService.listar());
        model.addAttribute("fecha", f);
        List<UbicacionGps> puntos = gpsService.recorrido(unidadId, f);
        model.addAttribute("puntos", puntos);
        model.addAttribute("linea", puntos.stream().map(p -> List.of(p.getLatitud(), p.getLongitud())).toList());
        return "gps/recorrido";
    }

    @GetMapping("/historial")
    public String historial(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", gpsService.historial(PageRequest.of(pagina, 30)));
        return "gps/historial";
    }

    @PostMapping("/registrar")
    public String registrar(@Valid @ModelAttribute("ubicacion") UbicacionGps ubicacion, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Revise la unidad y las coordenadas (latitud entre -90 y 90, longitud entre -180 y 180)");
            return "redirect:/gps";
        }
        gpsService.registrar(ubicacion);
        ra.addFlashAttribute("exito", "Posición registrada");
        return "redirect:/gps";
    }

    @PostMapping("/simular")
    public String simular(RedirectAttributes ra) {
        int n = gpsService.simular();
        ra.addFlashAttribute(n > 0 ? "exito" : "error",
                n > 0 ? "Se simularon " + n + " reportes GPS de unidades en servicio" : "No hay unidades en servicio para simular");
        return "redirect:/gps";
    }
}
