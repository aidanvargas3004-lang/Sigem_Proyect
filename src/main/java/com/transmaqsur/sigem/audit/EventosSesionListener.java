package com.transmaqsur.sigem.audit;

import com.transmaqsur.sigem.model.enums.AccionAuditoria;
import com.transmaqsur.sigem.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Registra en auditoría los inicios de sesión exitosos y fallidos. */
@Component
@RequiredArgsConstructor
public class EventosSesionListener {

    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;

    @EventListener
    public void exito(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        usuarioRepository.registrarAcceso(username, LocalDateTime.now());
        auditoriaService.registrar(username, AccionAuditoria.INICIO_SESION, "Usuario", null, "Inicio de sesión correcto");
    }

    @EventListener
    public void fallo(AbstractAuthenticationFailureEvent event) {
        auditoriaService.registrar(event.getAuthentication().getName(), AccionAuditoria.ACCESO_FALLIDO, "Usuario", null,
                "Intento fallido: " + event.getException().getMessage());
    }
}
