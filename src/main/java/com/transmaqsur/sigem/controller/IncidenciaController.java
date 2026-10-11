package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.dto.IncidenciaForm;
import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Incidencia;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.enums.EstadoIncidencia;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoIncidencia;
import com.transmaqsur.sigem.security.Seguridad;
import com.transmaqsur.sigem.security.UsuarioPrincipal;
import com.transmaqsur.sigem.service.AsignacionService;
import com.transmaqsur.sigem.service.IncidenciaService;
import com.transmaqsur.sigem.service.UnidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

import java.util.ArrayList;
import java.util.List;

/** Incidencias de servicio (RF011 / CU006 Reportar incidencia). */
@Controller
@RequestMapping("/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaService incidenciaService;
    private final AsignacionService asignacionService;
    private final UnidadService unidadService;

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoIncidencia estado, Model model) {
        model.addAttribute("incidencias", incidenciaService.listar().stream()
                .filter(i -> estado == null || i.getEstado() == estado).toList());
        model.addAttribute("estados", EstadoIncidencia.values());
        model.addAttribute("estado", estado);
        return "incidencias/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long asignacionId, Model model) {
        IncidenciaForm form = new IncidenciaForm();
        form.setAsignacionId(asignacionId);
        model.addAttribute("incidenciaForm", form);
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("incidencia", incidenciaService.obtener(id));
        model.addAttribute("puedeGenerarOrden", Seguridad.tienePermiso(Modulo.MANTENIMIENTO.getPermisoGestionar()));
        return "incidencias/detalle";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("incidenciaForm") IncidenciaForm form, BindingResult br, Model model,
                          RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Long empleadoId = Seguridad.usuarioActual().map(UsuarioPrincipal::getEmpleadoId).orElse(null);
            Incidencia i = incidenciaService.registrar(form, empleadoId);
            ra.addFlashAttribute("exito", "Incidencia " + i.getCodigo() + " reportada. Operaciones y Mantenimiento fueron notificados.");
            return "redirect:/incidencias/" + i.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/{id}/atender")
    public String atender(@PathVariable Long id, RedirectAttributes ra) {
        incidenciaService.atender(id);
        ra.addFlashAttribute("exito", "Incidencia en atención");
        return "redirect:/incidencias/" + id;
    }

    @PostMapping("/{id}/orden")
    public String generarOrden(@PathVariable Long id, RedirectAttributes ra) {
        if (!Seguridad.tienePermiso(Modulo.MANTENIMIENTO.getPermisoGestionar())) {
            throw new AccessDeniedException("Se requiere permiso de Mantenimiento");
        }
        OrdenMantenimiento o = incidenciaService.generarOrden(id);
        ra.addFlashAttribute("exito", "Se generó la orden correctiva " + o.getCodigo());
        return "redirect:/mantenimiento/" + o.getId();
    }

    @PostMapping("/{id}/resolver")
    public String resolver(@PathVariable Long id, @RequestParam String solucion, RedirectAttributes ra) {
        incidenciaService.resolver(id, solucion);
        ra.addFlashAttribute("exito", "Incidencia resuelta");
        return "redirect:/incidencias/" + id;
    }

    private String form(Model model) {
        List<Asignacion> activas = new ArrayList<>();
        Long empleadoId = Seguridad.usuarioActual().map(UsuarioPrincipal::getEmpleadoId).orElse(null);
        // Un conductor/operador ve sus trabajos; Operaciones ve todos los activos
        activas.addAll(empleadoId != null && !Seguridad.tienePermiso(Modulo.ASIGNACIONES.getPermisoVer())
                ? asignacionService.activasDeEmpleado(empleadoId) : asignacionService.listarActivas());
        model.addAttribute("asignaciones", activas);
        model.addAttribute("unidades", unidadService.listar());
        model.addAttribute("tipos", TipoIncidencia.values());
        model.addAttribute("severidades", Prioridad.values());
        return "incidencias/form";
    }
}
