package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.AlquilerRepository;
import com.transmaqsur.sigem.repository.ServicioTransporteRepository;
import com.transmaqsur.sigem.service.AsignacionService;
import com.transmaqsur.sigem.service.EmpleadoService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Asignación de maquinaria y personal, y consulta de disponibilidad. */
@Controller
@RequestMapping("/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService asignacionService;
    private final UnidadService unidadService;
    private final EmpleadoService empleadoService;
    private final AlquilerRepository alquilerRepository;
    private final ServicioTransporteRepository servicioRepository;

    @GetMapping
    public String listar(@RequestParam(defaultValue = "false") boolean historial, Model model) {
        model.addAttribute("asignaciones", historial ? asignacionService.listar() : asignacionService.listarActivas());
        model.addAttribute("historial", historial);
        return "asignaciones/lista";
    }

    /** Busca unidades y personal libres en un rango de fechas. */
    @GetMapping("/disponibilidad")
    public String disponibilidad(@RequestParam(required = false) LocalDateTime inicio, @RequestParam(required = false) LocalDateTime fin,
                                 @RequestParam(required = false) TipoUnidad tipo, Model model) {
        LocalDateTime i = inicio != null ? inicio : LocalDate.now().plusDays(1).atTime(7, 0);
        LocalDateTime f = fin != null ? fin : LocalDate.now().plusDays(1).atTime(18, 0);
        if (!f.isAfter(i)) {
            throw new NegocioException("La fecha de fin debe ser posterior a la de inicio");
        }
        model.addAttribute("inicio", i);
        model.addAttribute("fin", f);
        model.addAttribute("tipo", tipo);
        model.addAttribute("tipos", TipoUnidad.values());
        model.addAttribute("unidades", asignacionService.unidadesDisponibles(i, f, tipo));
        model.addAttribute("personal", asignacionService.personalDisponible(i, f));
        return "asignaciones/disponibilidad";
    }

    /** Lleva al documento que originó la asignación. */
    @GetMapping("/{id}")
    public String ver(@PathVariable Long id) {
        Asignacion a = asignacionService.obtener(id);
        return switch (a.getOrigen()) {
            case ALQUILER -> alquilerRepository.findByAsignacionId(id).map(x -> "redirect:/alquileres/" + x.getId()).orElse("redirect:/asignaciones");
            case SERVICIO -> servicioRepository.findByAsignacionId(id).map(x -> "redirect:/servicios/" + x.getId()).orElse("redirect:/asignaciones");
            case INTERNA -> "redirect:/asignaciones?historial=true";
        };
    }

    @GetMapping("/nuevo")
    public String nueva(Model model) {
        Asignacion a = new Asignacion();
        a.setFechaInicio(LocalDate.now().plusDays(1).atTime(8, 0));
        a.setFechaFin(LocalDate.now().plusDays(1).atTime(17, 0));
        model.addAttribute("asignacion", a);
        return form(model);
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("asignacion") Asignacion asignacion, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Asignacion a = asignacionService.crearInterna(asignacion);
            ra.addFlashAttribute("exito", "Asignación " + a.getReferencia() + " registrada");
            return "redirect:/asignaciones";
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, RedirectAttributes ra) {
        asignacionService.iniciarInterna(id);
        ra.addFlashAttribute("exito", "Asignación iniciada");
        return "redirect:/asignaciones";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, RedirectAttributes ra) {
        asignacionService.finalizarInterna(id);
        ra.addFlashAttribute("exito", "Asignación finalizada");
        return "redirect:/asignaciones";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        asignacionService.cancelarInterna(id);
        ra.addFlashAttribute("exito", "Asignación cancelada");
        return "redirect:/asignaciones";
    }

    private String form(Model model) {
        model.addAttribute("unidades", unidadService.listarOperativas());
        model.addAttribute("personal", empleadoService.listarPersonalDeCampo());
        return "asignaciones/form";
    }
}
