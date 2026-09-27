package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Rol;
import com.transmaqsur.sigem.model.Usuario;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.security.Seguridad;
import com.transmaqsur.sigem.service.EmpleadoService;
import com.transmaqsur.sigem.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Usuarios, roles y permisos. */
@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final EmpleadoService empleadoService;

    @InitBinder("usuario")
    void binder(WebDataBinder binder) {
        binder.setDisallowedFields("password", "ultimoAcceso");
    }

    // ------------------------------------------------------------ Usuarios

    @GetMapping("/usuarios")
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return form(model);
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.obtener(id));
        return form(model);
    }

    @PostMapping("/usuarios/guardar")
    public String guardar(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult br,
                          @RequestParam(required = false) String passwordNueva, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Usuario u = usuarioService.guardar(usuario, passwordNueva);
            ra.addFlashAttribute("exito", "Usuario " + u.getUsername() + " guardado");
            return "redirect:/usuarios";
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/usuarios/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes ra) {
        usuarioService.cambiarEstado(id, Seguridad.usuarioActual().orElseThrow().getUsername());
        ra.addFlashAttribute("exito", "Estado del usuario actualizado");
        return "redirect:/usuarios";
    }

    private String form(Model model) {
        model.addAttribute("roles", usuarioService.listarRoles());
        model.addAttribute("empleados", empleadoService.listarActivos());
        return "usuarios/form";
    }

    // ------------------------------------------------------------ Roles

    @GetMapping("/roles")
    public String roles(Model model) {
        model.addAttribute("roles", usuarioService.listarRoles());
        model.addAttribute("modulos", Modulo.values());
        return "usuarios/roles";
    }

    @GetMapping("/roles/nuevo")
    public String nuevoRol(Model model) {
        model.addAttribute("rol", new Rol());
        return formRol(model);
    }

    @GetMapping("/roles/{id}/editar")
    public String editarRol(@PathVariable Long id, Model model) {
        model.addAttribute("rol", usuarioService.obtenerRol(id));
        return formRol(model);
    }

    @PostMapping("/roles/guardar")
    public String guardarRol(@Valid @ModelAttribute("rol") Rol rol, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return formRol(model);
        }
        try {
            Rol r = usuarioService.guardarRol(rol);
            ra.addFlashAttribute("exito", "Rol " + r.getNombre() + " guardado. Los cambios aplican en el próximo inicio de sesión.");
            return "redirect:/roles";
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return formRol(model);
        }
    }

    @PostMapping("/roles/{id}/eliminar")
    public String eliminarRol(@PathVariable Long id, RedirectAttributes ra) {
        usuarioService.eliminarRol(id);
        ra.addFlashAttribute("exito", "Rol eliminado");
        return "redirect:/roles";
    }

    private String formRol(Model model) {
        model.addAttribute("modulos", Modulo.values());
        return "usuarios/rol-form";
    }
}
