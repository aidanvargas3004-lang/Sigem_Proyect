# Modelo de datos de SIGEM

La base de datos PostgreSQL tiene **30 tablas**, creadas automáticamente por Flyway con el
script [`V1__esquema_inicial.sql`](../src/main/resources/db/migration/V1__esquema_inicial.sql).
Todas las tablas de negocio incluyen los campos de trazabilidad `fecha_creacion`, `creado_por`,
`fecha_modificacion` y `modificado_por`.

## Diagrama entidad-relación (principal)

```mermaid
erDiagram
    ROL ||--o{ ROL_PERMISO : tiene
    ROL ||--o{ USUARIO : asigna
    EMPLEADO |o--o{ USUARIO : "vinculado a"
    NOTIFICACION }o--|| USUARIO : "dirigida a"

    CLIENTE ||--o{ CONTACTO : tiene
    CAMPANA |o--o{ OPORTUNIDAD : genera
    CLIENTE ||--o{ OPORTUNIDAD : tiene
    OPORTUNIDAD |o--o| SOLICITUD : "se convierte en"
    CLIENTE ||--o{ SOLICITUD : realiza
    SOLICITUD |o--o{ COTIZACION : "se cotiza en"
    COTIZACION ||--o{ COTIZACION_DETALLE : contiene
    COTIZACION |o--o| CONTRATO : origina
    CONTRATO |o--o| CONTRATO : "renueva a"
    CLIENTE ||--o{ CONTRATO : firma

    CONTRATO |o--o{ ALQUILER : agrupa
    CONTRATO |o--o{ SERVICIO_TRANSPORTE : agrupa
    ALQUILER ||--|| ASIGNACION : reserva
    SERVICIO_TRANSPORTE ||--|| ASIGNACION : reserva
    UNIDAD ||--o{ ASIGNACION : "se asigna en"
    EMPLEADO |o--o{ ASIGNACION : "conduce/opera"

    UNIDAD ||--o{ ORDEN_MANTENIMIENTO : recibe
    EMPLEADO |o--o{ ORDEN_MANTENIMIENTO : "técnico de"
    ORDEN_MANTENIMIENTO ||--o{ REPUESTO_UTILIZADO : consume
    UNIDAD ||--o{ UBICACION_GPS : reporta

    ALMACEN ||--o{ INVENTARIO : guarda
    REPUESTO ||--o{ INVENTARIO : "stock de"
    ALMACEN ||--o{ MOVIMIENTO_INVENTARIO : registra
    REPUESTO ||--o{ MOVIMIENTO_INVENTARIO : registra
    PROVEEDOR ||--o{ COMPRA : atiende
    COMPRA ||--o{ COMPRA_DETALLE : contiene
    REPUESTO ||--o{ COMPRA_DETALLE : "se compra en"

    CLIENTE ||--o{ VENTA : recibe
    VENTA ||--o{ VENTA_DETALLE : contiene
    VENTA_DETALLE }o--o| ALQUILER : factura
    VENTA_DETALLE }o--o| SERVICIO_TRANSPORTE : factura
    VENTA_DETALLE }o--o| REPUESTO : vende
```

La tabla `AUDITORIA` no tiene llaves foráneas a propósito: guarda el nombre de la entidad y su id
para conservar el historial aunque el registro original se elimine.

## Tablas por módulo

| Módulo | Tablas |
|---|---|
| Usuarios y permisos | `rol`, `rol_permiso`, `usuario` |
| Clientes y contactos | `cliente`, `contacto` |
| Marketing y oportunidades | `campana`, `oportunidad` |
| Maquinaria y flota | `unidad` |
| Empleados | `empleado` |
| Almacenes y repuestos | `almacen`, `repuesto`, `inventario`, `movimiento_inventario` |
| Compras | `proveedor`, `compra`, `compra_detalle` |
| Solicitudes y cotizaciones | `solicitud`, `cotizacion`, `cotizacion_detalle` |
| Contratos | `contrato` |
| Ventas | `venta`, `venta_detalle` |
| Alquileres | `alquiler` |
| Servicios de transporte | `servicio_transporte` |
| Asignación de maquinaria y empleados | `asignacion` |
| Mantenimiento | `orden_mantenimiento` |
| Repuestos utilizados | `repuesto_utilizado` |
| Ubicación GPS | `ubicacion_gps` |
| Notificaciones | `notificacion` |
| Auditoría y trazabilidad | `auditoria` |

## Estados principales

| Entidad | Estados |
|---|---|
| Unidad | DISPONIBLE → ASIGNADA → EN_SERVICIO → DISPONIBLE · EN_MANTENIMIENTO · FUERA_SERVICIO |
| Solicitud | REGISTRADA → COTIZADA → APROBADA → ATENDIDA · RECHAZADA · ANULADA |
| Cotización | BORRADOR → ENVIADA → ACEPTADA · RECHAZADA · VENCIDA |
| Contrato | VIGENTE → FINALIZADO · RENOVADO · ANULADO |
| Alquiler / servicio | PROGRAMADO → EN_CURSO → FINALIZADO · ANULADO |
| Asignación | PROGRAMADA → EN_CURSO → FINALIZADA · CANCELADA |
| Orden de mantenimiento | PROGRAMADA → EN_PROCESO → COMPLETADA · CANCELADA |
| Compra | PENDIENTE → APROBADA → RECIBIDA · ANULADA |
| Venta | BORRADOR → EMITIDA → PAGADA · ANULADA |

El estado de la **unidad** no se edita a mano: el sistema lo calcula a partir de sus asignaciones y
órdenes de mantenimiento (mantenimiento en proceso > en servicio > asignada > disponible).
