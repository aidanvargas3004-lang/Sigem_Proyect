package com.transur.sigem.seguridad.controller;

import com.transur.sigem.security.entity.Rol;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.RolRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/seguridad")
@RequiredArgsConstructor
public class SeguridadController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "seguridad/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String formUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", rolRepository.findAll());
        return "seguridad/usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam String username,
                                 @RequestParam String password,
                                 @RequestParam String email,
                                 @RequestParam String nombres,
                                 @RequestParam String apellidos,
                                 @RequestParam Long rolId,
                                 RedirectAttributes redirect) {
        try {
            if (usuarioRepository.existsByUsername(username)) throw new IllegalArgumentException("El usuario ya existe");
            if (usuarioRepository.existsByEmail(email)) throw new IllegalArgumentException("El correo ya existe");
            Rol rol = rolRepository.findById(rolId).orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
            Usuario usuario = new Usuario();
            usuario.setUsername(username.trim());
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setEmail(email.trim());
            usuario.setNombres(nombres.trim());
            usuario.setApellidos(apellidos.trim());
            usuario.setRol(rol);
            usuario.setEstado(true);
            usuario.setFechaCreacion(LocalDateTime.now());
            usuarioRepository.save(usuario);
            redirect.addFlashAttribute("success", "Usuario creado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seguridad/usuarios";
    }

    @PostMapping("/usuarios/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Usuario u = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            u.setEstado(!Boolean.TRUE.equals(u.getEstado()));
            usuarioRepository.save(u);
            redirect.addFlashAttribute("success", "Estado del usuario actualizado.");
        } catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/seguridad/usuarios";
    }

    @GetMapping("/roles")
    public String listarRoles(Model model) {
        model.addAttribute("roles", rolRepository.findAll());
        return "seguridad/roles";
    }

    @GetMapping("/roles/nuevo")
    public String formRol(Model model) {
        model.addAttribute("rol", new Rol());
        return "seguridad/rol-form";
    }

    @PostMapping("/roles/guardar")
    public String guardarRol(@ModelAttribute Rol rol, RedirectAttributes redirect) {
        try {
            if (rolRepository.findByNombre(rol.getNombre()).isPresent()) throw new IllegalArgumentException("El rol ya existe");
            rol.setEstado(true);
            rolRepository.save(rol);
            redirect.addFlashAttribute("success", "Rol creado correctamente.");
        } catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/seguridad/roles";
    }
}
