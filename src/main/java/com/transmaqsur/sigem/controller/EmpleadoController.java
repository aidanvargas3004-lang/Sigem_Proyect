package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.enums.AreaEmpresa;
import com.transmaqsur.sigem.model.enums.CargoEmpleado;
import com.transmaqsur.sigem.model.enums.EstadoEmpleado;
import com.transmaqsur.sigem.service.AsignacionService;
import com.transmaqsur.sigem.service.EmpleadoService;
import com.transmaqsur.sigem.service.MantenimientoService;
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

@Controller
@RequestMapping("/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final AsignacionService asignacionService;
    private final MantenimientoService mantenimientoService;

    @GetMapping
    public String listar(@RequestParam(required = false) AreaEmpresa area, Model model) {
        model.addAttribute("empleados", empleadoService.listar().stream().filter(e -> area == null || e.getArea() == area).toList());
        model.addAttribute("areas", AreaEmpresa.values());
        model.addAttribute("area", area);
        model.addAttribute("limiteLicencia", LocalDate.now().plusDays(30));
        return "empleados/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("empleado", new Empleado());
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("empleado", empleadoService.obtener(id));
        model.addAttribute("asignaciones", asignacionService.deEmpleado(id));
        model.addAttribute("ordenes", mantenimientoService.listar().stream()
                .filter(o -> o.getTecnico() != null && o.getTecnico().getId().equals(id)).toList());
        return "empleados/detalle";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("empleado") Empleado empleado, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Empleado e = empleadoService.guardar(empleado);
            ra.addFlashAttribute("exito", "Empleado " + e.getNombreCompleto() + " guardado");
            return "redirect:/empleados/" + e.getId();
        } catch (NegocioException ex) {
            br.reject("negocio", ex.getMessage());
            return form(model);
        }
    }

    private String form(Model model) {
        model.addAttribute("cargos", CargoEmpleado.values());
        model.addAttribute("areas", AreaEmpresa.values());
        model.addAttribute("estados", EstadoEmpleado.values());
        return "empleados/form";
    }
}
