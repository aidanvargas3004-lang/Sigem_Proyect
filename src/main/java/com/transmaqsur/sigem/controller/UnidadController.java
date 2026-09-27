package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.UbicacionGpsRepository;
import com.transmaqsur.sigem.service.AsignacionService;
import com.transmaqsur.sigem.service.MantenimientoService;
import com.transmaqsur.sigem.service.UnidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/** Maquinaria y flota. */
@Controller
@RequestMapping("/unidades")
@RequiredArgsConstructor
public class UnidadController {

    private final UnidadService unidadService;
    private final AsignacionService asignacionService;
    private final MantenimientoService mantenimientoService;
    private final UbicacionGpsRepository gpsRepository;

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoUnidad estado, @RequestParam(required = false) TipoUnidad tipo, Model model) {
        List<Unidad> unidades = unidadService.listar().stream()
                .filter(u -> estado == null || u.getEstado() == estado)
                .filter(u -> tipo == null || u.getTipo() == tipo).toList();
        model.addAttribute("unidades", unidades);
        model.addAttribute("estados", EstadoUnidad.values());
        model.addAttribute("tipos", TipoUnidad.values());
        model.addAttribute("estado", estado);
        model.addAttribute("tipo", tipo);
        return "unidades/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("unidad", new Unidad());
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("unidad", unidadService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("unidad", unidadService.obtener(id));
        model.addAttribute("asignaciones", asignacionService.deUnidad(id));
        model.addAttribute("ordenes", mantenimientoService.deUnidad(id));
        model.addAttribute("ultimaPosicion", gpsRepository.findFirstByUnidadIdOrderByFechaHoraDesc(id).orElse(null));
        return "unidades/detalle";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("unidad") Unidad unidad, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Unidad u = unidadService.guardar(unidad);
            ra.addFlashAttribute("exito", "Unidad " + u.getCodigo() + " guardada");
            return "redirect:/unidades/" + u.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/{id}/lectura")
    public String registrarLectura(@PathVariable Long id, @RequestParam BigDecimal lectura, RedirectAttributes ra) {
        unidadService.registrarLectura(id, lectura);
        ra.addFlashAttribute("exito", "Lectura registrada");
        return "redirect:/unidades/" + id;
    }

    @PostMapping("/{id}/fuera-servicio")
    public String fueraDeServicio(@PathVariable Long id, RedirectAttributes ra) {
        unidadService.cambiarFueraDeServicio(id);
        ra.addFlashAttribute("exito", "Estado de la unidad actualizado");
        return "redirect:/unidades/" + id;
    }

    private String form(Model model) {
        model.addAttribute("tipos", TipoUnidad.values());
        return "unidades/form";
    }
}
