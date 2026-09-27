package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Campana;
import com.transmaqsur.sigem.model.Oportunidad;
import com.transmaqsur.sigem.model.Solicitud;
import com.transmaqsur.sigem.model.enums.CanalMarketing;
import com.transmaqsur.sigem.model.enums.EstadoCampana;
import com.transmaqsur.sigem.model.enums.EtapaOportunidad;
import com.transmaqsur.sigem.model.enums.TipoServicio;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.MarketingService;
import com.transmaqsur.sigem.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Campañas de marketing y oportunidades comerciales. */
@Controller
@RequiredArgsConstructor
public class MarketingController {

    private final MarketingService marketingService;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    // ------------------------------------------------------------ Campañas

    @GetMapping("/campanas")
    public String campanas(Model model) {
        model.addAttribute("campanas", marketingService.listarCampanas());
        return "marketing/campanas";
    }

    @GetMapping("/campanas/nuevo")
    public String nuevaCampana(Model model) {
        model.addAttribute("campana", new Campana());
        return formCampana(model);
    }

    @GetMapping("/campanas/{id}/editar")
    public String editarCampana(@PathVariable Long id, Model model) {
        model.addAttribute("campana", marketingService.obtenerCampana(id));
        return formCampana(model);
    }

    @GetMapping("/campanas/{id}")
    public String detalleCampana(@PathVariable Long id, Model model) {
        model.addAttribute("campana", marketingService.obtenerCampana(id));
        List<Oportunidad> ops = marketingService.oportunidadesDeCampana(id);
        model.addAttribute("oportunidades", ops);
        model.addAttribute("montoTotal", ops.stream().map(Oportunidad::getMontoEstimado).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("ganadas", ops.stream().filter(o -> o.getEtapa() == EtapaOportunidad.GANADA).count());
        return "marketing/campana-detalle";
    }

    @PostMapping("/campanas/guardar")
    public String guardarCampana(@Valid @ModelAttribute("campana") Campana campana, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return formCampana(model);
        }
        try {
            Campana c = marketingService.guardarCampana(campana);
            ra.addFlashAttribute("exito", "Campaña guardada");
            return "redirect:/campanas/" + c.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return formCampana(model);
        }
    }

    private String formCampana(Model model) {
        model.addAttribute("canales", CanalMarketing.values());
        model.addAttribute("estados", EstadoCampana.values());
        return "marketing/campana-form";
    }

    // ------------------------------------------------------------ Oportunidades

    @GetMapping("/oportunidades")
    public String oportunidades(Model model) {
        List<Oportunidad> todas = marketingService.listarOportunidades();
        Map<EtapaOportunidad, List<Oportunidad>> embudo = new LinkedHashMap<>();
        Arrays.stream(EtapaOportunidad.values()).forEach(e -> embudo.put(e, todas.stream().filter(o -> o.getEtapa() == e).toList()));
        model.addAttribute("embudo", embudo);
        model.addAttribute("oportunidades", todas);
        model.addAttribute("pipeline", todas.stream()
                .filter(o -> o.getEtapa() != EtapaOportunidad.GANADA && o.getEtapa() != EtapaOportunidad.PERDIDA)
                .map(Oportunidad::getMontoPonderado).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("etapas", EtapaOportunidad.values());
        return "marketing/oportunidades";
    }

    @GetMapping("/oportunidades/nuevo")
    public String nuevaOportunidad(@RequestParam(required = false) Long campanaId, Model model) {
        Oportunidad o = new Oportunidad();
        if (campanaId != null) {
            o.setCampana(marketingService.obtenerCampana(campanaId));
        }
        model.addAttribute("oportunidad", o);
        return formOportunidad(model);
    }

    @GetMapping("/oportunidades/{id}/editar")
    public String editarOportunidad(@PathVariable Long id, Model model) {
        model.addAttribute("oportunidad", marketingService.obtenerOportunidad(id));
        return formOportunidad(model);
    }

    @PostMapping("/oportunidades/guardar")
    public String guardarOportunidad(@Valid @ModelAttribute("oportunidad") Oportunidad oportunidad, BindingResult br, Model model,
                                     RedirectAttributes ra) {
        if (br.hasErrors()) {
            return formOportunidad(model);
        }
        marketingService.guardarOportunidad(oportunidad);
        ra.addFlashAttribute("exito", "Oportunidad guardada");
        return "redirect:/oportunidades";
    }

    @PostMapping("/oportunidades/{id}/etapa")
    public String cambiarEtapa(@PathVariable Long id, @RequestParam EtapaOportunidad etapa, RedirectAttributes ra) {
        marketingService.cambiarEtapa(id, etapa);
        ra.addFlashAttribute("exito", "Oportunidad movida a " + etapa.getLabel());
        return "redirect:/oportunidades";
    }

    @PostMapping("/oportunidades/{id}/solicitud")
    public String generarSolicitud(@PathVariable Long id, RedirectAttributes ra) {
        Solicitud s = marketingService.generarSolicitud(id);
        ra.addFlashAttribute("exito", "Se generó la solicitud " + s.getCodigo() + ". Revise las fechas y detalles antes de cotizar.");
        return "redirect:/solicitudes/" + s.getId();
    }

    private String formOportunidad(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("campanas", marketingService.listarCampanas());
        model.addAttribute("usuarios", usuarioService.listarActivos());
        model.addAttribute("etapas", EtapaOportunidad.values());
        model.addAttribute("tiposServicio", TipoServicio.values());
        return "marketing/oportunidad-form";
    }
}
