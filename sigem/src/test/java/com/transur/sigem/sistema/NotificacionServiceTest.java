package com.transur.sigem.sistema;

import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.entity.Notificacion;
import com.transur.sigem.sistema.repository.NotificacionRepository;
import com.transur.sigem.sistema.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** RF014: las alertas automáticas no se duplican y cada usuario solo marca sus notificaciones. */
@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock NotificacionRepository repo;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks NotificacionService service;

    private static Usuario usuario(long id) {
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    @Test
    void noDuplicaUnaAlertaConLaMismaClave() {
        when(repo.existsByUsuarioIdAndClave(1L, "PLAN-3")).thenReturn(true);
        assertThat(service.notificar(usuario(1), "t", "m", "WARNING", "PLAN-3", "/x")).isFalse();
        verify(repo, never()).save(any());
    }

    @Test
    void creaLaAlertaCuandoEsNueva() {
        when(repo.existsByUsuarioIdAndClave(1L, "PLAN-3")).thenReturn(false);
        assertThat(service.notificar(usuario(1), "t", "m", "WARNING", "PLAN-3", "/x")).isTrue();
        verify(repo).save(any(Notificacion.class));
    }

    @Test
    void unUsuarioNoPuedeMarcarNotificacionesAjenas() {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario(2));
        when(repo.findById(9L)).thenReturn(Optional.of(n));
        assertThatThrownBy(() -> service.marcarLeidaDeUsuario(9L, 1L)).isInstanceOf(RecursoNoEncontradoException.class);
        assertThat(n.getLeida()).isFalse();
    }
}
