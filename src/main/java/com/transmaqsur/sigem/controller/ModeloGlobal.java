package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Notificacion;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.security.Seguridad;
import com.transmaqsur.sigem.security.UsuarioPrincipal;
import com.transmaqsur.sigem.service.NotificacionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.ArrayList;
import java.util.List;

import static com.transmaqsur.sigem.model.enums.Modulo.*;

/** Datos comunes a todas las pantallas: menú según permisos, notificaciones y errores de negocio. */
@ControllerAdvice(basePackages = "com.transmaqsur.sigem.controller")
@RequiredArgsConstructor
public class ModeloGlobal {

    public record ItemMenu(String etiqueta, String icono, String url, Modulo modulo) {
    }

    public record SeccionMenu(String titulo, List<ItemMenu> items) {
    }

    private static final List<SeccionMenu> MENU = List.of(
            new SeccionMenu("Comercial", List.of(
                    new ItemMenu("Clientes", "bi-building", "/clientes", CLIENTES),
                    new ItemMenu("Campañas", "bi-megaphone", "/campanas", MARKETING),
                    new ItemMenu("Oportunidades", "bi-funnel", "/oportunidades", MARKETING),
                    new ItemMenu("Solicitudes", "bi-inbox", "/solicitudes", SOLICITUDES),
                    new ItemMenu("Cotizaciones", "bi-file-earmark-text", "/cotizaciones", SOLICITUDES),
                    new ItemMenu("Contratos", "bi-file-earmark-check", "/contratos", CONTRATOS),
                    new ItemMenu("Ventas", "bi-receipt", "/ventas", VENTAS))),
            new SeccionMenu("Operaciones", List.of(
                    new ItemMenu("Alquileres", "bi-calendar2-range", "/alquileres", ALQUILERES),
                    new ItemMenu("Servicios de transporte", "bi-truck", "/servicios", SERVICIOS),
                    new ItemMenu("Asignaciones", "bi-diagram-3", "/asignaciones", ASIGNACIONES),
                    new ItemMenu("Ubicación GPS", "bi-geo-alt", "/gps", GPS))),
            new SeccionMenu("Flota y mantenimiento", List.of(
                    new ItemMenu("Maquinaria y flota", "bi-truck-front", "/unidades", FLOTA),
                    new ItemMenu("Mantenimiento", "bi-tools", "/mantenimiento", MANTENIMIENTO),
                    new ItemMenu("Repuestos utilizados", "bi-nut", "/mantenimiento/repuestos-utilizados", MANTENIMIENTO))),
            new SeccionMenu("Logística", List.of(
                    new ItemMenu("Inventario", "bi-boxes", "/inventario", ALMACENES),
                    new ItemMenu("Repuestos", "bi-gear-wide-connected", "/repuestos", ALMACENES),
                    new ItemMenu("Almacenes", "bi-house-gear", "/almacenes", ALMACENES),
                    new ItemMenu("Compras", "bi-cart3", "/compras", COMPRAS),
                    new ItemMenu("Proveedores", "bi-shop", "/proveedores", COMPRAS))),
            new SeccionMenu("Recursos humanos", List.of(
                    new ItemMenu("Empleados", "bi-people", "/empleados", EMPLEADOS))),
            new SeccionMenu("Administración", List.of(
                    new ItemMenu("Reportes", "bi-bar-chart-line", "/reportes", REPORTES),
                    new ItemMenu("Usuarios", "bi-person-badge", "/usuarios", USUARIOS),
                    new ItemMenu("Roles y permisos", "bi-shield-lock", "/roles", USUARIOS),
                    new ItemMenu("Auditoría", "bi-clock-history", "/auditoria", AUDITORIA))));

    private final NotificacionService notificacionService;

    @ModelAttribute("menu")
    public List<SeccionMenu> menu() {
        List<SeccionMenu> visible = new ArrayList<>();
        for (SeccionMenu s : MENU) {
            List<ItemMenu> items = s.items().stream().filter(i -> Seguridad.tienePermiso(i.modulo().getPermisoVer())).toList();
            if (!items.isEmpty()) {
                visible.add(new SeccionMenu(s.titulo(), items));
            }
        }
        return visible;
    }

    /** URL del ítem de menú que corresponde a la página actual (el prefijo más largo). */
    @ModelAttribute("menuActivo")
    public String menuActivo(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return MENU.stream().flatMap(s -> s.items().stream()).map(ItemMenu::url)
                .filter(u -> uri.equals(u) || uri.startsWith(u + "/"))
                .max(java.util.Comparator.comparingInt(String::length)).orElse(uri.equals("/") ? "/" : "");
    }

    @ModelAttribute("uriActual")
    public String uriActual(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("usuarioActual")
    public UsuarioPrincipal usuarioActual() {
        return Seguridad.usuarioActual().orElse(null);
    }

    @ModelAttribute("notificacionesNoLeidas")
    public long notificacionesNoLeidas() {
        return Seguridad.usuarioActual().map(u -> notificacionService.contarNoLeidas(u.getUsuarioId())).orElse(0L);
    }

    @ModelAttribute("notificacionesRecientes")
    public List<Notificacion> notificacionesRecientes() {
        return Seguridad.usuarioActual().map(u -> notificacionService.recientes(u.getUsuarioId())).orElse(List.of());
    }

    /** Errores de regla de negocio en acciones: vuelve a la página anterior mostrando el mensaje. */
    @ExceptionHandler(NegocioException.class)
    public String negocio(NegocioException ex, HttpServletRequest request) {
        FlashMap flash = RequestContextUtils.getOutputFlashMap(request);
        flash.put("error", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/");
    }
}
