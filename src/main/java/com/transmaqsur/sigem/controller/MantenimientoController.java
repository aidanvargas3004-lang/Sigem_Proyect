package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.enums.EstadoOrden;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoMantenimiento;
import com.transmaqsur.sigem.service.EmpleadoService;
import com.transmaqsur.sigem.service.InventarioService;
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
import java.time.LocalDate;

/** Órdenes de mantenimiento, alertas preventivas y repuestos utilizados. */
@Controller
@RequestMapping("/mantenimiento")
@RequiredArgsConstructor
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;
    private final UnidadService unidadService;
    private final EmpleadoService empleadoService;
    private final InventarioService inventarioService;

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoOrden estado, Model model) {
        model.addAttribute("ordenes", mantenimientoService.listar().stream().filter(o -> estado == null || o.getEstado() == estado).toList());
        model.addAttribute("alertas", unidadService.requierenMantenimiento());
        model.addAttribute("estados", EstadoOrden.values());
        model.addAttribute("estado", estado);
        return "mantenimiento/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long unidadId, Model model) {
        OrdenMantenimiento o;
        if (unidadId != null) {
            o = mantenimientoService.nuevaPreventiva(unidadId);
        } else {
            o = new OrdenMantenimiento();
            o.setFechaProgramada(LocalDate.now().plusDays(1));
        }
        model.addAttribute("orden", o);
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("orden", mantenimientoService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        OrdenMantenimiento o = mantenimientoService.obtener(id);
        model.addAttribute("orden", o);
        if (o.getEstado() == EstadoOrden.EN_PROCESO) {
            model.addAttribute("stock", inventarioService.conStock());
        }
        return "mantenimiento/detalle";
    }

    @GetMapping("/repuestos-utilizados")
    public String repuestosUtilizados(Model model) {
        var lista = mantenimientoService.repuestosUtilizados();
        model.addAttribute("repuestos", lista);
        model.addAttribute("total", lista.stream().map(r -> r.getSubtotal()).reduce(BigDecimal.ZERO, BigDecimal::add));
        return "mantenimiento/repuestos-utilizados";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("orden") OrdenMantenimiento orden, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            OrdenMantenimiento o = mantenimientoService.guardar(orden);
            ra.addFlashAttribute("exito", "Orden " + o.getCodigo() + " programada");
            return "redirect:/mantenimiento/" + o.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, RedirectAttributes ra) {
        mantenimientoService.iniciar(id);
        ra.addFlashAttribute("exito", "Orden en proceso: la unidad quedó en mantenimiento y bloqueada para asignaciones");
        return "redirect:/mantenimiento/" + id;
    }

    @PostMapping("/{id}/repuestos")
    public String agregarRepuesto(@PathVariable Long id, @RequestParam String inventario, @RequestParam BigDecimal cantidad,
                                  RedirectAttributes ra) {
        String[] partes = inventario.split("-");
        if (partes.length != 2) {
            throw new NegocioException("Seleccione el repuesto y almacén");
        }
        mantenimientoService.agregarRepuesto(id, Long.valueOf(partes[0]), Long.valueOf(partes[1]), cantidad);
        ra.addFlashAttribute("exito", "Repuesto registrado y descontado del almacén");
        return "redirect:/mantenimiento/" + id;
    }

    @PostMapping("/{id}/repuestos/{ruId}/devolver")
    public String devolverRepuesto(@PathVariable Long id, @PathVariable Long ruId, RedirectAttributes ra) {
        mantenimientoService.quitarRepuesto(id, ruId);
        ra.addFlashAttribute("exito", "Repuesto devuelto al almacén");
        return "redirect:/mantenimiento/" + id;
    }

    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Long id, @RequestParam String trabajoRealizado,
                            @RequestParam(required = false) BigDecimal costoManoObra,
                            @RequestParam(required = false) BigDecimal costoTerceros,
                            @RequestParam(required = false) BigDecimal lectura, RedirectAttributes ra) {
        mantenimientoService.completar(id, trabajoRealizado, costoManoObra, costoTerceros, lectura);
        ra.addFlashAttribute("exito", "Mantenimiento completado: la unidad está disponible nuevamente");
        return "redirect:/mantenimiento/" + id;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        mantenimientoService.cancelar(id);
        ra.addFlashAttribute("exito", "Orden cancelada");
        return "redirect:/mantenimiento/" + id;
    }

    private String form(Model model) {
        model.addAttribute("unidades", unidadService.listar());
        model.addAttribute("tecnicos", empleadoService.listarTecnicos());
        model.addAttribute("tipos", TipoMantenimiento.values());
        model.addAttribute("prioridades", Prioridad.values());
        return "mantenimiento/form";
    }
}
