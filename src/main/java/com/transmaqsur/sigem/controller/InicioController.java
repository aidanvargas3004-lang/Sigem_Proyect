package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.security.Seguridad;
import com.transmaqsur.sigem.security.UsuarioPrincipal;
import com.transmaqsur.sigem.service.AsignacionService;
import com.transmaqsur.sigem.service.DashboardService;
import com.transmaqsur.sigem.service.MantenimientoService;
import com.transmaqsur.sigem.service.UnidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InicioController {

    private final DashboardService dashboardService;
    private final AsignacionService asignacionService;
    private final MantenimientoService mantenimientoService;
    private final UnidadService unidadService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("r", dashboardService.resumen());
        return "dashboard";
    }

    /** Asignaciones y órdenes de trabajo del empleado vinculado al usuario (conductores, operadores, técnicos). */
    @GetMapping("/mis-tareas")
    public String misTareas(Model model) {
        UsuarioPrincipal u = Seguridad.usuarioActual().orElseThrow();
        if (u.getEmpleadoId() != null) {
            model.addAttribute("asignaciones", asignacionService.activasDeEmpleado(u.getEmpleadoId()));
            model.addAttribute("historial", asignacionService.deEmpleado(u.getEmpleadoId()).stream().limit(15).toList());
            model.addAttribute("ordenes", mantenimientoService.abiertasDeTecnico(u.getEmpleadoId()));
            model.addAttribute("unidades", unidadService.listarOperativas());
        } else {
            model.addAttribute("asignaciones", List.of());
            model.addAttribute("historial", List.of());
            model.addAttribute("ordenes", List.of());
        }
        return "mis-tareas";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "error/403";
    }
}
