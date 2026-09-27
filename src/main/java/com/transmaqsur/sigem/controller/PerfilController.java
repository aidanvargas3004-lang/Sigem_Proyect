package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.security.Seguridad;
import com.transmaqsur.sigem.security.UsuarioPrincipal;
import com.transmaqsur.sigem.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.transmaqsur.sigem.service.NotificacionService;

@Controller
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    @GetMapping("/perfil")
    public String perfil(Model model) {
        model.addAttribute("usuario", usuarioService.obtener(Seguridad.usuarioActual().orElseThrow().getUsuarioId()));
        return "perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(@RequestParam String actual, @RequestParam String nueva, @RequestParam String confirmacion,
                                  RedirectAttributes ra) {
        try {
            usuarioService.cambiarPasswordPropia(Seguridad.usuarioActual().orElseThrow().getUsuarioId(), actual, nueva, confirmacion);
            ra.addFlashAttribute("exito", "Contraseña actualizada correctamente");
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/perfil";
    }

    // ------------------------------------------------------------ Notificaciones del usuario

    @GetMapping("/notificaciones")
    public String notificaciones(@RequestParam(defaultValue = "0") int pagina, Model model) {
        UsuarioPrincipal u = Seguridad.usuarioActual().orElseThrow();
        model.addAttribute("pagina", notificacionService.listar(u.getUsuarioId(), PageRequest.of(pagina, 20)));
        return "notificaciones";
    }

    @GetMapping("/notificaciones/{id}/abrir")
    public String abrir(@PathVariable Long id) {
        String enlace = notificacionService.marcarLeida(id, Seguridad.usuarioActual().orElseThrow().getUsuarioId());
        return "redirect:" + (enlace != null && enlace.startsWith("/") ? enlace : "/notificaciones");
    }

    @PostMapping("/notificaciones/leer-todas")
    public String leerTodas(RedirectAttributes ra) {
        notificacionService.marcarTodasLeidas(Seguridad.usuarioActual().orElseThrow().getUsuarioId());
        ra.addFlashAttribute("exito", "Todas las notificaciones fueron marcadas como leídas");
        return "redirect:/notificaciones";
    }
}
