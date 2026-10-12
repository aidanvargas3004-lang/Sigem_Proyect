package com.transur.sigem.sistema.controller;

import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.service.AlertaService;
import com.transur.sigem.sistema.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Bandeja de notificaciones del usuario autenticado (RF014). */
@Controller
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionViewController {

    private final NotificacionService notificacionService;
    private final AlertaService alertaService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String bandeja(Authentication auth, Model model) {
        Usuario u = usuario(auth);
        model.addAttribute("notificaciones", notificacionService.listarRecientes(u.getId()));
        model.addAttribute("usuario", u);
        model.addAttribute("esAdmin", "ADMIN".equals(u.getRol().getNombre()));
        return "sistema/notificaciones";
    }

    @PostMapping("/{id}/leer")
    public String leer(@PathVariable Long id, Authentication auth) {
        String enlace = notificacionService.marcarLeidaDeUsuario(id, usuario(auth).getId());
        // Solo se redirige a rutas internas para evitar redirecciones abiertas.
        return "redirect:" + (enlace != null && enlace.startsWith("/") && !enlace.startsWith("//") ? enlace : "/notificaciones");
    }

    @PostMapping("/leer-todas")
    public String leerTodas(Authentication auth) {
        notificacionService.marcarTodasComoLeidas(usuario(auth).getId());
        return "redirect:/notificaciones";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generar")
    public String generar(RedirectAttributes ra) {
        ra.addFlashAttribute("ok", "Revisión completada: " + alertaService.generarAlertas() + " alerta(s) nueva(s).");
        return "redirect:/notificaciones";
    }

    private Usuario usuario(Authentication auth) {
        return usuarioRepository.findByUsername(auth.getName()).orElseThrow();
    }
}
