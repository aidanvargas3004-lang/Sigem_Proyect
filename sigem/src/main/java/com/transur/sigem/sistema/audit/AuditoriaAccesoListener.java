package com.transur.sigem.sistema.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.transur.sigem.sistema.dao.AuditoriaDao;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** Registra en la bitácora los inicios de sesión exitosos y fallidos. */
@Component
@RequiredArgsConstructor
public class AuditoriaAccesoListener {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final AuditoriaDao auditoriaDao;

    @EventListener
    public void exito(AuthenticationSuccessEvent e) {
        if (e.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            auditoriaDao.insertar(e.getAuthentication().getName(), "SEGURIDAD", "LOGIN", "Usuario", null, null, null);
        }
    }

    @EventListener
    public void fallo(AbstractAuthenticationFailureEvent e) {
        String usuario = String.valueOf(e.getAuthentication().getPrincipal());
        ObjectNode datos = JSON.createObjectNode()
                .put("usuario", usuario.length() > 50 ? usuario.substring(0, 50) : usuario)
                .put("motivo", e.getException().getClass().getSimpleName());
        auditoriaDao.insertar(null, "SEGURIDAD", "LOGIN_FALLIDO", "Usuario", null, null, datos.toString());
    }
}
