package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Alquiler;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.security.Seguridad;
import com.transmaqsur.sigem.service.AlquilerService;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.ContratoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Alquiler de maquinaria. */
@Controller
@RequestMapping("/alquileres")
@RequiredArgsConstructor
public class AlquilerController {

    private final AlquilerService alquilerService;
    private final ClienteService clienteService;
    private final ContratoService contratoService;
    private final UnidadService unidadService;
    private final EmpleadoService empleadoService;

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoOperacion estado, Model model) {
        model.addAttribute("alquileres", alquilerService.listar().stream().filter(a -> estado == null || a.getEstado() == estado).toList());
        model.addAttribute("estados", EstadoOperacion.values());
        model.addAttribute("estado", estado);
        return "operaciones/alquileres";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long contratoId, @RequestParam(required = false) Long unidadId, Model model) {
        Alquiler a = new Alquiler();
        a.setModalidad(Modalidad.POR_DIA);
        a.getAsignacion().setFechaInicio(LocalDate.now().plusDays(1).atTime(7, 0));
        a.getAsignacion().setFechaFin(LocalDate.now().plusDays(7).atTime(18, 0));
        if (contratoId != null) {
            a.setContrato(contratoService.obtener(contratoId));
            a.setCliente(a.getContrato().getCliente());
        }
        if (unidadId != null) {
            a.getAsignacion().setUnidad(unidadService.obtener(unidadId));
        }
        model.addAttribute("alquiler", a);
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("alquiler", alquilerService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("op", alquilerService.obtener(id));
        model.addAttribute("ahora", LocalDateTime.now().withSecond(0).withNano(0));
        model.addAttribute("puedeGestionar", Seguridad.tienePermiso(Modulo.ALQUILERES.getPermisoGestionar()));
        return "operaciones/alquiler-detalle";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("alquiler") Alquiler alquiler, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Alquiler a = alquilerService.guardar(alquiler);
            ra.addFlashAttribute("exito", "Alquiler " + a.getCodigo() + " programado. La unidad quedó reservada.");
            return "redirect:/alquileres/" + a.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, @RequestParam(required = false) BigDecimal lectura,
                          @RequestParam(required = false) LocalDateTime fechaHora, RedirectAttributes ra) {
        alquilerService.iniciar(id, lectura, fechaHora);
        ra.addFlashAttribute("exito", "Alquiler iniciado: la unidad está en servicio");
        return "redirect:/alquileres/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, @RequestParam(required = false) BigDecimal lectura,
                            @RequestParam(required = false) LocalDateTime fechaHora, RedirectAttributes ra) {
        alquilerService.finalizar(id, lectura, fechaHora);
        ra.addFlashAttribute("exito", "Alquiler finalizado y valorizado. Ya puede facturarse.");
        return "redirect:/alquileres/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes ra) {
        alquilerService.anular(id, motivo);
        ra.addFlashAttribute("exito", "Alquiler anulado: la unidad fue liberada");
        return "redirect:/alquileres/" + id;
    }

    private String form(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("contratos", contratoService.listarVigentes());
        model.addAttribute("unidades", unidadService.listarOperativas());
        model.addAttribute("operadores", empleadoService.listarPersonalDeCampo());
        model.addAttribute("modalidades", List.of(Modalidad.POR_HORA, Modalidad.POR_DIA));
        return "operaciones/alquiler-form";
    }
}
