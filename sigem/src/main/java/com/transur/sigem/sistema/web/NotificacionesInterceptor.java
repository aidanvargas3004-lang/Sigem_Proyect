package com.transur.sigem.sistema.web;

import com.transur.sigem.common.util.UsuarioActual;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.repository.NotificacionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Agrega a toda vista HTML el número de notificaciones pendientes del usuario
 * autenticado, que el fragmento {@code fragments/campana} muestra como insignia.
 */
@Component
@RequiredArgsConstructor
public class NotificacionesInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mav) {
        if (mav == null || mav.getViewName() == null || mav.getViewName().startsWith("redirect:")) return;
        UsuarioActual.username()
                .flatMap(usuarioRepository::findByUsername)
                .ifPresent(u -> mav.addObject("notifPendientes", notificacionRepository.countByUsuarioIdAndLeidaFalse(u.getId())));
    }
}
