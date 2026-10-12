package com.transur.sigem.mantenimiento.controller;

import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.mantenimiento.dto.MantenimientoRequest;
import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import com.transur.sigem.mantenimiento.service.MantenimientoService;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Vistas del módulo de mantenimiento (RF012, RF013) dentro del portal de Operaciones. */
@Controller
@RequestMapping("/operaciones/mantenimiento")
@RequiredArgsConstructor
public class MantenimientoViewController {

    private final MantenimientoService mantenimientoService;
    private final MaquinariaRepository maquinariaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RepuestoRepository repuestoRepository;

    @GetMapping
    public String ordenes(@RequestParam(required = false) String estado, Model model) {
        List<Mantenimiento> ordenes = mantenimientoService.listarOrdenes().stream()
                .filter(m -> estado == null || estado.isBlank() || estado.equals(m.getEstado())).toList();
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("estado", estado);
        model.addAttribute("planesAlerta", mantenimientoService.listarPlanes().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()) && !"OK".equals(p.getSituacion())).toList());
        model.addAttribute("maquinarias", maquinariaRepository.findAll());
        model.addAttribute("tecnicos", empleadoRepository.findAll().stream()
                .filter(e -> Boolean.TRUE.equals(e.getEstado())).toList());
        model.addAttribute("hoy", LocalDate.now());
        return "operaciones/mantenimiento";
    }

    @PostMapping
    public String programar(@ModelAttribute MantenimientoRequest request, RedirectAttributes redirect) {
        try {
            request.setEstado("PROGRAMADO");
            request.setRepuestosUsados(null);
            var r = mantenimientoService.crear(request);
            redirect.addFlashAttribute("mensaje", "Orden MNT-" + r.getId() + " programada.");
            return "redirect:/operaciones/mantenimiento/" + r.getId();
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", "No se pudo programar: " + e.getMessage());
            return "redirect:/operaciones/mantenimiento";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("m", mantenimientoService.obtener(id));
        model.addAttribute("repuestos", repuestoRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getEstado()) && r.getStockActual() != null && r.getStockActual() > 0).toList());
        return "operaciones/mantenimiento-detalle";
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, RedirectAttributes redirect) {
        return accion(id, redirect, "Orden en proceso: la unidad quedó EN MANTENIMIENTO.", () -> mantenimientoService.iniciar(id));
    }

    @PostMapping("/{id}/repuestos")
    public String agregarRepuesto(@PathVariable Long id, @RequestParam Long repuestoId, @RequestParam Integer cantidad,
                                  @RequestParam BigDecimal costoUnitario, RedirectAttributes redirect) {
        return accion(id, redirect, "Repuesto registrado y descontado del almacén.",
                () -> mantenimientoService.agregarRepuesto(id, repuestoId, cantidad, costoUnitario));
    }

    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Long id, @RequestParam String trabajoRealizado,
                            @RequestParam(required = false) BigDecimal costoManoObra,
                            @RequestParam(required = false) BigDecimal horometro, RedirectAttributes redirect) {
        return accion(id, redirect, "Mantenimiento completado: la unidad está disponible.",
                () -> mantenimientoService.completar(id, trabajoRealizado, costoManoObra, horometro));
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect) {
        return accion(id, redirect, "Orden cancelada.", () -> mantenimientoService.cancelar(id));
    }

    @PostMapping("/desde-incidencia/{incidenciaId}")
    public String desdeIncidencia(@PathVariable Long incidenciaId, RedirectAttributes redirect) {
        try {
            Mantenimiento m = mantenimientoService.generarDesdeIncidencia(incidenciaId);
            redirect.addFlashAttribute("mensaje", "Orden correctiva MNT-" + m.getId() + " generada desde la incidencia.");
            return "redirect:/operaciones/mantenimiento/" + m.getId();
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/operaciones/checklist";
        }
    }

    // ------------------------------------------------------------ Planes preventivos

    @GetMapping("/planes")
    public String planes(Model model) {
        model.addAttribute("planes", mantenimientoService.listarPlanes());
        model.addAttribute("maquinarias", maquinariaRepository.findAll());
        return "operaciones/planes";
    }

    @PostMapping("/planes")
    public String crearPlan(@RequestParam Long maquinariaId, @RequestParam String actividad, @RequestParam String criterio,
                            @RequestParam BigDecimal frecuencia, @RequestParam(required = false) BigDecimal ultimoValor,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ultimaFecha,
                            RedirectAttributes redirect) {
        try {
            mantenimientoService.crearPlan(maquinariaId, actividad, criterio, frecuencia, ultimoValor, ultimaFecha);
            redirect.addFlashAttribute("mensaje", "Plan preventivo registrado.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/operaciones/mantenimiento/planes";
    }

    @PostMapping("/planes/{planId}/orden")
    public String ordenDesdePlan(@PathVariable Long planId, RedirectAttributes redirect) {
        try {
            Mantenimiento m = mantenimientoService.generarDesdePlan(planId);
            redirect.addFlashAttribute("mensaje", "Orden preventiva MNT-" + m.getId() + " programada.");
            return "redirect:/operaciones/mantenimiento/" + m.getId();
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/operaciones/mantenimiento/planes";
        }
    }

    @PostMapping("/planes/{planId}/estado")
    public String estadoPlan(@PathVariable Long planId, RedirectAttributes redirect) {
        mantenimientoService.cambiarEstadoPlan(planId);
        redirect.addFlashAttribute("mensaje", "Estado del plan actualizado.");
        return "redirect:/operaciones/mantenimiento/planes";
    }

    private String accion(Long id, RedirectAttributes redirect, String ok, Runnable r) {
        try {
            r.run();
            redirect.addFlashAttribute("mensaje", ok);
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/operaciones/mantenimiento/" + id;
    }
}
