package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.service.AlquilerService;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.ContratoService;
import com.transmaqsur.sigem.service.ServicioTransporteService;
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

@Controller
@RequestMapping("/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;
    private final ClienteService clienteService;
    private final AlquilerService alquilerService;
    private final ServicioTransporteService servicioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("contratos", contratoService.listar());
        return "contratos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long clienteId, Model model) {
        Contrato c = new Contrato();
        c.setFechaInicio(LocalDate.now());
        c.setFechaFin(LocalDate.now().plusMonths(3));
        if (clienteId != null) {
            c.setCliente(clienteService.obtener(clienteId));
        }
        model.addAttribute("contrato", c);
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("contrato", contratoService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("contrato", contratoService.obtener(id));
        model.addAttribute("alquileres", alquilerService.deContrato(id));
        model.addAttribute("servicios", servicioService.deContrato(id));
        return "contratos/detalle";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("contrato") Contrato contrato, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Contrato c = contratoService.guardar(contrato);
            ra.addFlashAttribute("exito", "Contrato " + c.getCodigo() + " guardado");
            return "redirect:/contratos/" + c.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/desde-cotizacion/{cotizacionId}")
    public String desdeCotizacion(@PathVariable Long cotizacionId, RedirectAttributes ra) {
        Contrato c = contratoService.generarDesdeCotizacion(cotizacionId);
        ra.addFlashAttribute("exito", "Contrato " + c.getCodigo() + " generado desde la cotización");
        return "redirect:/contratos/" + c.getId();
    }

    @PostMapping("/{id}/renovar")
    public String renovar(@PathVariable Long id, @RequestParam LocalDate fechaFin, @RequestParam(required = false) BigDecimal monto,
                          RedirectAttributes ra) {
        Contrato c = contratoService.renovar(id, fechaFin, monto);
        ra.addFlashAttribute("exito", "Contrato renovado: se creó " + c.getCodigo());
        return "redirect:/contratos/" + c.getId();
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, RedirectAttributes ra) {
        contratoService.finalizar(id);
        ra.addFlashAttribute("exito", "Contrato finalizado");
        return "redirect:/contratos/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        contratoService.anular(id);
        ra.addFlashAttribute("exito", "Contrato anulado");
        return "redirect:/contratos/" + id;
    }

    private String form(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        return "contratos/form";
    }
}
