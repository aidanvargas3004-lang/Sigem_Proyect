package com.transur.sigem.sistema.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transur.sigem.common.util.UsuarioActual;
import com.transur.sigem.sistema.dao.AuditoriaDao;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * RF018 - Auditoría automática: registra cada alta, modificación y baja de las entidades
 * del sistema con el usuario autenticado y los valores anteriores/nuevos (en JSON).
 * Escribe mediante {@link AuditoriaDao} (JDBC) dentro de la misma transacción del negocio.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaEntidadesListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    /** Entidades que no se auditan: la propia bitácora y datos de alto volumen o efímeros. */
    private static final Set<String> EXCLUIDAS = Set.of("Auditoria", "Notificacion", "UbicacionGps");
    private static final Set<String> SENSIBLES = Set.of("password", "contrasena", "token");
    private static final int MAX_TEXTO = 500;

    private final EntityManagerFactory entityManagerFactory;
    private final AuditoriaDao auditoriaDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    void registrar() {
        EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, this);
        registry.appendListeners(EventType.POST_UPDATE, this);
        registry.appendListeners(EventType.POST_DELETE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent e) {
        guardar("CREAR", e.getEntity(), e.getId(), null, valores(e.getPersister(), e.getState(), null));
    }

    @Override
    public void onPostUpdate(PostUpdateEvent e) {
        int[] sucias = e.getDirtyProperties();
        Map<String, Object> antes = valores(e.getPersister(), e.getOldState(), sucias);
        Map<String, Object> despues = valores(e.getPersister(), e.getState(), sucias);
        if (Objects.equals(antes, despues)) return;
        guardar("ACTUALIZAR", e.getEntity(), e.getId(), antes, despues);
    }

    @Override
    public void onPostDelete(PostDeleteEvent e) {
        guardar("ELIMINAR", e.getEntity(), e.getId(), valores(e.getPersister(), e.getDeletedState(), null), null);
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private void guardar(String accion, Object entidad, Object id, Map<String, Object> antes, Map<String, Object> despues) {
        String nombre = entidad.getClass().getSimpleName();
        if (EXCLUIDAS.contains(nombre)) return;
        try {
            auditoriaDao.insertar(UsuarioActual.username().orElse(null), modulo(entidad), accion, nombre,
                    id instanceof Number n ? n.longValue() : null, json(antes), json(despues));
        } catch (RuntimeException ex) {
            log.error("No se pudo registrar auditoría de {} #{}: {}", nombre, id, ex.getMessage());
        }
    }

    /** com.transur.sigem.<modulo>.entity.Clase → MODULO */
    static String modulo(Object entidad) {
        String[] partes = entidad.getClass().getPackageName().split("\\.");
        return partes.length >= 4 ? partes[3].toUpperCase() : "SISTEMA";
    }

    private Map<String, Object> valores(EntityPersister persister, Object[] estado, int[] indices) {
        if (estado == null) return null;
        String[] nombres = persister.getPropertyNames();
        Map<String, Object> mapa = new LinkedHashMap<>();
        if (indices != null) {
            for (int i : indices) poner(mapa, nombres[i], estado[i]);
        } else {
            for (int i = 0; i < nombres.length; i++) poner(mapa, nombres[i], estado[i]);
        }
        return mapa;
    }

    private void poner(Map<String, Object> mapa, String propiedad, Object valor) {
        if (valor instanceof PersistentCollection<?> || valor instanceof Collection<?>) return;
        if (SENSIBLES.contains(propiedad.toLowerCase())) {
            mapa.put(propiedad, "******");
            return;
        }
        mapa.put(propiedad, simple(valor));
    }

    /** Las relaciones se guardan solo por su id para no inicializar proxies perezosos. */
    static Object simple(Object valor) {
        if (valor == null) return null;
        if (valor instanceof HibernateProxy proxy) {
            return proxy.getHibernateLazyInitializer().getIdentifier();
        }
        if (valor.getClass().isAnnotationPresent(Entity.class)) {
            try {
                return valor.getClass().getMethod("getId").invoke(valor);
            } catch (ReflectiveOperationException ex) {
                return valor.getClass().getSimpleName();
            }
        }
        if (valor instanceof Number || valor instanceof Boolean) return valor;
        String texto = valor.toString();
        return texto.length() > MAX_TEXTO ? texto.substring(0, MAX_TEXTO) + "…" : texto;
    }

    private String json(Map<String, Object> mapa) {
        if (mapa == null || mapa.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(mapa);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
