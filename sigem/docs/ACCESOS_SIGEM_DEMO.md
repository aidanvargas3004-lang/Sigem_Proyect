# SIGEM — Cuentas de acceso DEMO

> **Uso:** entorno de demostración/desarrollo. Las contraseñas que aparecen aquí son las definidas por el `RoleBootstrap` y `DemoDataSeeder` del proyecto.
>
> **Importante:** no usar estas credenciales en producción.

## 1. Cuentas internas por rol

| Rol | Usuario | Contraseña | Correo |
|---|---|---|---|
| ADMIN | `admin` | `Admin123*` | `admin@transur.com` |
| GERENCIA | `gerente` | `Gerente123*` | `gerente@transur.com` |
| VENTAS | `ventas` | `Ventas123*` | `ventas@transur.com` |
| MARKETING | `marketing` | `Marketing123*` | `marketing@transur.com` |
| OPERACIONES | `operaciones` | `Operaciones123*` | `operaciones@transur.com` |
| RRHH | `rrhh` | `Rrhh123*` | `rrhh@transur.com` |
| LOGISTICA | `logistica` | `Logistica123*` | `logistica@transur.com` |

## 2. Cuenta cliente base

| Rol | Usuario | Contraseña | Correo | Documento cliente |
|---|---|---|---|---|
| CLIENTE | `cliente` | `Cliente123*` | `cliente@transur.com` | `20600000001` |

Cliente asociado: **Constructora Demo SAC**.

## 3. Clientes DEMO adicionales

El `DemoDataSeeder` crea 20 clientes con acceso al portal. Todos usan la misma contraseña DEMO.

| # | Usuario | Contraseña | Correo | Cliente | Documento |
|---:|---|---|---|---|---|
| 01 | `cliente01` | `Cliente123*` | `cliente1@demo.transur.com` | Constructora Demo 1 SAC | 20610000001 |
| 02 | `cliente02` | `Cliente123*` | `cliente2@demo.transur.com` | Constructora Demo 2 SAC | 20610000002 |
| 03 | `cliente03` | `Cliente123*` | `cliente3@demo.transur.com` | Constructora Demo 3 SAC | 20610000003 |
| 04 | `cliente04` | `Cliente123*` | `cliente4@demo.transur.com` | Cliente Natural Demo 4 | 20610000004 |
| 05 | `cliente05` | `Cliente123*` | `cliente5@demo.transur.com` | Constructora Demo 5 SAC | 20610000005 |
| 06 | `cliente06` | `Cliente123*` | `cliente6@demo.transur.com` | Constructora Demo 6 SAC | 20610000006 |
| 07 | `cliente07` | `Cliente123*` | `cliente7@demo.transur.com` | Constructora Demo 7 SAC | 20610000007 |
| 08 | `cliente08` | `Cliente123*` | `cliente8@demo.transur.com` | Cliente Natural Demo 8 | 20610000008 |
| 09 | `cliente09` | `Cliente123*` | `cliente9@demo.transur.com` | Constructora Demo 9 SAC | 20610000009 |
| 10 | `cliente10` | `Cliente123*` | `cliente10@demo.transur.com` | Constructora Demo 10 SAC | 20610000010 |
| 11 | `cliente11` | `Cliente123*` | `cliente11@demo.transur.com` | Constructora Demo 11 SAC | 20610000011 |
| 12 | `cliente12` | `Cliente123*` | `cliente12@demo.transur.com` | Cliente Natural Demo 12 | 20610000012 |
| 13 | `cliente13` | `Cliente123*` | `cliente13@demo.transur.com` | Constructora Demo 13 SAC | 20610000013 |
| 14 | `cliente14` | `Cliente123*` | `cliente14@demo.transur.com` | Constructora Demo 14 SAC | 20610000014 |
| 15 | `cliente15` | `Cliente123*` | `cliente15@demo.transur.com` | Constructora Demo 15 SAC | 20610000015 |
| 16 | `cliente16` | `Cliente123*` | `cliente16@demo.transur.com` | Cliente Natural Demo 16 | 20610000016 |
| 17 | `cliente17` | `Cliente123*` | `cliente17@demo.transur.com` | Constructora Demo 17 SAC | 20610000017 |
| 18 | `cliente18` | `Cliente123*` | `cliente18@demo.transur.com` | Constructora Demo 18 SAC | 20610000018 |
| 19 | `cliente19` | `Cliente123*` | `cliente19@demo.transur.com` | Constructora Demo 19 SAC | 20610000019 |
| 20 | `cliente20` | `Cliente123*` | `cliente20@demo.transur.com` | Cliente Natural Demo 20 | 20610000020 |

## 4. Permisos principales por rol

Los permisos se asignan desde `RoleBootstrap`.

### ADMIN
- Dashboard
- Usuarios
- Roles
- Clientes
- Marketing
- Ventas
- Maquinaria
- Servicios
- RRHH
- Logística
- Mantenimiento
- Reportes
- Portal Cliente

### CLIENTE
- Portal Cliente

### VENTAS
- Dashboard
- Clientes
- Ventas

### MARKETING
- Dashboard
- Clientes
- Marketing

### OPERACIONES
- Dashboard
- Maquinaria
- Servicios
- Mantenimiento

### RRHH
- Dashboard
- RRHH

### LOGISTICA
- Dashboard
- Logística

### GERENCIA
- Dashboard
- Reportes

## 5. Datos comerciales DEMO añadidos

Esta versión del proyecto también carga datos para que las secciones comerciales no aparezcan vacías:

- Ventas de maquinaria DEMO (`VTA-DEMO-...`) vinculadas a contratos de tipo `VENTA`.
- Detalles de venta con maquinaria, precio, cantidad y subtotal.
- Estados de venta variados: `COMPLETADA`, `ENTREGADA` y `GARANTIA_ACTIVA`.
- Alquileres DEMO vinculados a contratos de tipo `ALQUILER`.
- Detalles de alquiler con maquinaria, tarifa diaria, unidades de tiempo y subtotal.
- Estados de alquiler variados: `PROGRAMADO`, `EN_CURSO` y `FINALIZADO`.
- Los registros se generan de forma idempotente usando identificadores DEMO para no duplicar las ventas/alquileres al reiniciar la aplicación.

## 6. Notas

- Las cuentas de cliente `cliente01` a `cliente20` pertenecen al rol `CLIENTE`.
- La cuenta `cliente` es la cuenta cliente base creada por `RoleBootstrap`.
- Las cuentas internas (`admin`, `gerente`, `ventas`, etc.) son creadas por `RoleBootstrap`.
- Si una cuenta ya existe, `RoleBootstrap` no reemplaza su contraseña existente.
- El escenario DEMO está pensado para pruebas y presentación del sistema.
