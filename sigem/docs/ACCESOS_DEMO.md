# Accesos demo de SIGEM

## Cuentas principales

| Rol | Usuario | Clave |
|---|---|---|
| ADMIN | admin | Admin123* |
| GERENCIA | gerente | Gerente123* |
| VENTAS | ventas | Ventas123* |
| MARKETING | marketing | Marketing123* |
| OPERACIONES | operaciones | Operaciones123* |
| RRHH | rrhh | Rrhh123* |
| LOGISTICA | logistica | Logistica123* |
| CLIENTE | cliente | Cliente123* |

## Clientes demo adicionales

Se generan 20 cuentas adicionales para poblar solicitudes, cotizaciones y portal cliente:

- `cliente01` ... `cliente20`
- Clave común: `Cliente123*`
- Correo: `cliente01@demo.transur.com` ... `cliente20@demo.transur.com`

El `DemoDataSeeder` es idempotente: al volver a iniciar una base limpia crea el escenario demo; si ya existe, evita duplicarlo.
