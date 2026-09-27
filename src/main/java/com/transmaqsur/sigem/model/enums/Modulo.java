package com.transmaqsur.sigem.model.enums;

import lombok.Getter;

/**
 * Módulos del sistema. Cada módulo genera dos permisos:
 * {@code MODULO_VER} (consultar) y {@code MODULO_GESTIONAR} (registrar, editar y ejecutar acciones).
 * Las rutas indican qué URLs protege cada módulo.
 */
@Getter
public enum Modulo {
    USUARIOS("Usuarios y permisos", "/usuarios", "/roles"),
    CLIENTES("Clientes y contactos", "/clientes"),
    MARKETING("Marketing y oportunidades", "/campanas", "/oportunidades"),
    FLOTA("Maquinaria y flota", "/unidades"),
    EMPLEADOS("Empleados", "/empleados"),
    ALMACENES("Almacenes y repuestos", "/almacenes", "/repuestos", "/inventario"),
    COMPRAS("Compras y proveedores", "/proveedores", "/compras"),
    SOLICITUDES("Solicitudes y cotizaciones", "/solicitudes", "/cotizaciones"),
    CONTRATOS("Contratos", "/contratos"),
    VENTAS("Ventas y facturación", "/ventas"),
    ALQUILERES("Alquileres", "/alquileres"),
    SERVICIOS("Servicios de transporte", "/servicios"),
    ASIGNACIONES("Asignación de unidades y personal", "/asignaciones"),
    MANTENIMIENTO("Mantenimiento", "/mantenimiento"),
    GPS("Ubicación GPS", "/gps"),
    REPORTES("Reportes", "/reportes"),
    AUDITORIA("Auditoría y trazabilidad", "/auditoria");

    private final String label;
    private final String[] rutas;

    Modulo(String label, String... rutas) {
        this.label = label;
        this.rutas = rutas;
    }

    public String getPermisoVer() {
        return name() + "_VER";
    }

    public String getPermisoGestionar() {
        return name() + "_GESTIONAR";
    }
}
