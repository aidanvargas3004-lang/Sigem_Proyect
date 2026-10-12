package com.transur.sigem.operaciones.controller;

import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.dto.UbicacionGpsRequest;
import com.transur.sigem.operaciones.entity.UbicacionGps;
import com.transur.sigem.operaciones.service.UbicacionGpsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/** Mapa de la flota con la última posición informada de cada unidad. */
@Controller
@RequestMapping("/operaciones/gps")
@RequiredArgsConstructor
public class GpsViewController {

    private final UbicacionGpsService gpsService;
    private final MaquinariaRepository maquinariaRepository;

    @GetMapping
    public String mapa(@RequestParam(required = false) Long maquinariaId, Model model) {
        List<UbicacionGps> ultimas = gpsService.ultimasPosiciones();
        model.addAttribute("ultimas", ultimas);
        model.addAttribute("marcadores", ultimas.stream().map(g -> Map.of(
                "codigo", g.getMaquinaria().getCodigoInterno(), "estado", String.valueOf(g.getMaquinaria().getEstadoOperativo()),
                "lat", g.getLatitud(), "lon", g.getLongitud(), "ref", g.getReferencia() == null ? "" : g.getReferencia())).toList());
        model.addAttribute("maquinarias", maquinariaRepository.findAll());
        if (maquinariaId != null) {
            model.addAttribute("maquinariaId", maquinariaId);
            model.addAttribute("recorrido", gpsService.recorrido(maquinariaId).stream()
                    .map(g -> List.of(g.getLatitud(), g.getLongitud())).toList());
        }
        return "operaciones/gps";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute UbicacionGpsRequest request, BindingResult br,
                            @RequestParam(required = false) String volver, RedirectAttributes redirect) {
        if (br.hasErrors()) {
            redirect.addFlashAttribute("error", "Revise la unidad y las coordenadas (latitud −90 a 90, longitud −180 a 180).");
        } else {
            try {
                gpsService.registrar(request);
                redirect.addFlashAttribute("mensaje", "Posición registrada.");
            } catch (RuntimeException e) {
                redirect.addFlashAttribute("error", e.getMessage());
            }
        }
        return "redirect:" + (volver != null && volver.startsWith("/operaciones/") ? volver : "/operaciones/gps");
    }
}
