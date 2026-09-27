package com.transmaqsur.sigem.audit;

import com.transmaqsur.sigem.config.JpaAuditoriaConfig;
import com.transmaqsur.sigem.model.Auditoria;
import com.transmaqsur.sigem.model.enums.AccionAuditoria;
import com.transmaqsur.sigem.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserta el registro con JDBC en la misma conexión/transacción en curso.
     * Se usa también desde los listeners de Hibernate, donde no se puede usar el EntityManager.
     */
    public void registrar(AccionAuditoria accion, String entidad, Long entidadId, String detalle) {
        registrar(JpaAuditoriaConfig.usuarioActual(), accion, entidad, entidadId, detalle);
    }

    public void registrar(String usuario, AccionAuditoria accion, String entidad, Long entidadId, String detalle) {
        jdbcTemplate.update("insert into auditoria (fecha, usuario, accion, entidad, entidad_id, detalle, ip) values (?,?,?,?,?,?,?)",
                Timestamp.valueOf(LocalDateTime.now()), recortar(usuario, 50), accion.name(), entidad, entidadId,
                recortar(detalle, 4000), ipActual());
    }

    @Transactional(readOnly = true)
    public Page<Auditoria> buscar(String usuario, String entidad, AccionAuditoria accion,
                                  LocalDate desde, LocalDate hasta, Pageable pageable) {
        String u = vacioANull(usuario);
        String e = vacioANull(entidad);
        // Solo se agregan al filtro los criterios que tienen valor
        Specification<Auditoria> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (u != null) {
                p.add(cb.like(cb.lower(root.get("usuario")), "%" + u.toLowerCase() + "%"));
            }
            if (e != null) {
                p.add(cb.equal(root.get("entidad"), e));
            }
            if (accion != null) {
                p.add(cb.equal(root.get("accion"), accion));
            }
            if (desde != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde.atStartOfDay()));
            }
            if (hasta != null) {
                p.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta.atTime(LocalTime.MAX)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        return auditoriaRepository.findAll(spec, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"))));
    }

    @Transactional(readOnly = true)
    public List<Auditoria> historial(String entidad, Long id) {
        return auditoriaRepository.findByEntidadAndEntidadIdOrderByFechaDesc(entidad, id);
    }

    @Transactional(readOnly = true)
    public List<String> entidades() {
        return auditoriaRepository.entidades();
    }

    private static String vacioANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String recortar(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String ipActual() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest req = attrs.getRequest();
            String fwd = req.getHeader("X-Forwarded-For");
            return recortar(fwd != null ? fwd.split(",")[0].trim() : req.getRemoteAddr(), 45);
        }
        return null;
    }
}
