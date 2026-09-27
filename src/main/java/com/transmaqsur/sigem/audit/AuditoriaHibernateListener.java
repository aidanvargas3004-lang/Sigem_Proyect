package com.transmaqsur.sigem.audit;

import com.transmaqsur.sigem.model.EntidadBase;
import com.transmaqsur.sigem.model.SinAuditoria;
import com.transmaqsur.sigem.model.enums.AccionAuditoria;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Registra automáticamente en la tabla de auditoría toda creación, modificación
 * (con valores anteriores y nuevos) y eliminación de entidades del negocio.
 */
@Component
@RequiredArgsConstructor
public class AuditoriaHibernateListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private static final Set<String> IGNORAR = Set.of("fechaCreacion", "creadoPor", "fechaModificacion", "modificadoPor");
    private static final Set<String> OCULTAR = Set.of("password");

    private final EntityManagerFactory entityManagerFactory;
    private final AuditoriaService auditoriaService;

    @PostConstruct
    void registrarListeners() {
        EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, this);
        registry.appendListeners(EventType.POST_UPDATE, this);
        registry.appendListeners(EventType.POST_DELETE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (!auditable(event.getEntity())) {
            return;
        }
        String[] nombres = event.getPersister().getPropertyNames();
        Object[] estado = event.getState();
        StringJoiner sj = new StringJoiner("; ");
        for (int i = 0; i < nombres.length; i++) {
            if (!IGNORAR.contains(nombres[i]) && estado[i] != null && !(estado[i] instanceof Collection<?>)) {
                sj.add(nombres[i] + "=" + formatear(nombres[i], estado[i]));
            }
        }
        registrar(AccionAuditoria.CREAR, event.getEntity(), event.getId(), sj.toString());
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (!auditable(event.getEntity()) || event.getOldState() == null) {
            return;
        }
        String[] nombres = event.getPersister().getPropertyNames();
        Object[] antes = event.getOldState();
        Object[] despues = event.getState();
        StringJoiner sj = new StringJoiner("; ");
        for (int i = 0; i < nombres.length; i++) {
            if (IGNORAR.contains(nombres[i]) || despues[i] instanceof PersistentCollection<?>
                    || despues[i] instanceof Collection<?>) {
                continue;
            }
            String a = formatear(nombres[i], antes[i]);
            String d = formatear(nombres[i], despues[i]);
            if (!Objects.equals(a, d)) {
                sj.add(nombres[i] + ": " + a + " → " + d);
            }
        }
        if (sj.length() > 0) {
            registrar(AccionAuditoria.ACTUALIZAR, event.getEntity(), event.getId(), sj.toString());
        }
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (auditable(event.getEntity())) {
            registrar(AccionAuditoria.ELIMINAR, event.getEntity(), event.getId(), "Registro eliminado");
        }
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    private boolean auditable(Object entidad) {
        return entidad instanceof EntidadBase && !entidad.getClass().isAnnotationPresent(SinAuditoria.class);
    }

    private void registrar(AccionAuditoria accion, Object entidad, Object id, String detalle) {
        Long idLong = id instanceof Number n ? n.longValue() : null;
        auditoriaService.registrar(accion, entidad.getClass().getSimpleName(), idLong, detalle);
    }

    private static String formatear(String propiedad, Object valor) {
        if (valor == null) {
            return "∅";
        }
        if (OCULTAR.contains(propiedad)) {
            return "******";
        }
        if (valor instanceof EntidadBase e) {
            return e.getClass().getSimpleName() + "#" + e.getId();
        }
        if (valor instanceof BigDecimal b) {
            return b.stripTrailingZeros().toPlainString();
        }
        String s = valor.toString();
        return s.length() > 120 ? s.substring(0, 117) + "..." : s;
    }
}
