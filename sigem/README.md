# SIGEM — Sistema Integral de Gestión de Flotas y Maquinaria

Aplicación web de **TRANSUR S.A.C.** para gestionar de punta a punta el negocio de alquiler, venta y servicios con maquinaria pesada: clientes y marketing, solicitudes, cotizaciones, contratos, ventas y alquileres, programación y ejecución de servicios, asignación de unidades y operadores, mantenimiento preventivo/correctivo, almacén y compras, personal, ubicación GPS, notificaciones, reportes y auditoría.

| Capa | Tecnología |
|---|---|
| Lenguaje / framework | Java 21, Spring Boot 3.2.5 (Spring MVC, Spring Data JPA, Spring Security, Validation, Scheduling) |
| Vistas | Thymeleaf + Bootstrap 5 / Bootstrap Icons, Leaflet (mapa GPS) |
| Base de datos | PostgreSQL 16 (administrable con pgAdmin 4) |
| Acceso a datos | Repositorios JPA (DAO) y DAO JDBC para reportes y auditoría |
| Seguridad | Sesión con formulario + JWT para la API, BCrypt, roles por área, CSRF |
| Reportes | Apache POI (Excel) y OpenPDF (PDF) |
| Pruebas | JUnit 5, Mockito, AssertJ, Spring MockMvc, spring-security-test |
| IDE sugerido | Visual Studio Code con *Extension Pack for Java* y *Spring Boot Extension Pack* |

## 1. Puesta en marcha

### 1.1 Base de datos (pgAdmin 4)

1. En pgAdmin 4: *Servers → PostgreSQL → Databases → Create → Database…* con nombre `sigem_db`.
2. No hace falta ejecutar scripts: al arrancar, Hibernate crea/actualiza las tablas (`ddl-auto=update`), `RoleBootstrap` crea roles y cuentas y `DemoDataSeeder` / `DemoOperacionesSeeder` cargan un escenario demo completo.

Alternativa con Docker: `docker compose up -d postgres` (usuario y clave `postgres`).

### 1.2 Variables de entorno

Ver `.env.example`. Las principales:

| Variable | Valor por defecto | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/sigem_db` | Conexión JDBC |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `root112233` | Credenciales de PostgreSQL (cámbielas por las de su pgAdmin) |
| `JWT_SECRET` | clave de desarrollo | **Obligatorio cambiarla en producción** (32+ caracteres). La app avisa en el log si es débil |
| `CLOUDINARY_*` | vacío | Opcional, para subir imágenes |

### 1.3 Ejecutar

```bash
# Linux / macOS
DB_PASSWORD=su_clave ./mvnw spring-boot:run
# Windows (PowerShell)
$env:DB_PASSWORD="su_clave"; .\mvnw.cmd spring-boot:run
```

En VS Code también puede usar *Spring Boot Dashboard → sigem → Run*. Abra `http://localhost:8080`.

### 1.4 Cuentas demo

| Rol | Usuario | Clave | Panel |
|---|---|---|---|
| Administrador | `admin` | `Admin123*` | `/dashboard`, usuarios, roles, auditoría |
| Gerencia | `gerente` | `Gerente123*` | `/gerencia`, reportes operativos |
| Ventas | `ventas` | `Ventas123*` | `/ventas` |
| Marketing | `marketing` | `Marketing123*` | `/marketing/dashboard` |
| Operaciones / Producción | `operaciones` | `Operaciones123*` | `/operaciones` |
| Logística / Almacén | `logistica` | `Logistica123*` | `/logistica` |
| RRHH | `rrhh` | `Rrhh123*` | `/rrhh` |
| Cliente | `cliente` | `Cliente123*` | `/cliente` (portal) |

Más cuentas de clientes en `docs/ACCESOS_DEMO.md`.

### 1.5 Pruebas

```bash
./mvnw test                                   # 30 pruebas unitarias (no necesitan BD)
DB_URL=jdbc:postgresql://localhost:5432/sigem_db DB_PASSWORD=su_clave ./mvnw test   # + 11 de integración (MockMvc + PostgreSQL)
```

## 2. Módulos y requerimientos

| Área | Rutas | Funcionalidad principal |
|---|---|---|
| Administración | `/dashboard`, `/seguridad/*` | Usuarios, roles, **bitácora de auditoría** con filtros y exportación (RF018) |
| Marketing | `/marketing/*` | Clientes, campañas, leads, oportunidades, reporte Excel/PDF |
| Ventas / Comercial | `/ventas/*` | Solicitudes, cotizaciones, contratos, ventas, alquileres, reporte de ventas |
| Operaciones / Producción | `/operaciones/*` | Servicios, programación, maquinaria y tarifas, operadores, **asignación con validación de conflictos** (RF009), **ejecución con horómetro/km** (RF010), checklist e incidencias, **mantenimiento y planes preventivos** (RF012-RF013), **GPS** |
| Reportes operativos | `/reportes` | Servicios realizados, uso de flota, mantenimientos, incidencias, disponibilidad; Excel y PDF (RF016-RF017) |
| Logística / Almacén | `/logistica/*` | Almacenes, repuestos, stock, kardex de movimientos, órdenes de compra, recepción, inventario de maquinaria |
| RRHH | `/rrhh/*` | Empleados, documentación y licencias, vencimientos, historial de alertas |
| Gerencia | `/gerencia/*` | Indicadores, rentabilidad, operaciones, reportes ejecutivos |
| Portal cliente | `/cliente/*` | Solicitudes, cotizaciones, contratos y servicios del cliente |
| Transversal | `/notificaciones`, campana en todas las vistas | **Alertas automáticas** por rol (RF014) |
| API REST | `/auth/*`, `/api/*` | JWT; DTOs validados; errores JSON uniformes |

## 3. Estructura

```
src/main/java/com/transur/sigem
├── common        excepciones de negocio, manejador global de la API, exportadores de reportes, utilidades
├── security      autenticación (formulario + JWT), usuarios, roles, permisos
├── seguridad     vistas de administración de usuarios y roles
├── sistema       notificaciones, alertas automáticas, auditoría (listener + DAO JDBC), interceptor de vistas
├── cliente       clientes y portal del cliente
├── marketing     campañas, leads, oportunidades, reportes
├── comercial     solicitudes, cotizaciones, contratos, ventas, alquileres
├── maquinaria    tipos, unidades, tarifas
├── operaciones   servicios, asignaciones, ejecución, incidencias, GPS, reportes operativos (DAO JDBC)
├── mantenimiento órdenes de mantenimiento, repuestos usados, planes preventivos
├── logistica     almacenes, repuestos, movimientos, inventario
├── compras       proveedores, órdenes de compra, recepciones
├── activos       adquisiciones de maquinaria
├── rrhh          empleados y documentos
├── gerencia      tableros gerenciales
└── config        carga de datos base y demo
```

Cada módulo sigue la misma organización: `entity` (modelo JPA), `repository` (DAO), `dto` (Request/Response), `service` (reglas de negocio), `controller` (vistas MVC y API REST) y `templates/<área>` (vistas Thymeleaf).

## 4. Documentación técnica

- [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) — MVC, DTO, DAO, SOLID y otros patrones, por área.
- [docs/INVENTARIO_DOCUMENTACION.md](docs/INVENTARIO_DOCUMENTACION.md) — documentos que gestiona y emite cada área.
- [docs/SEGURIDAD.md](docs/SEGURIDAD.md) — controles de seguridad implementados y consideraciones por área.
- [docs/evidencias/](docs/evidencias/) — capturas de cada pantalla por rol, tomadas con la aplicación en ejecución sobre PostgreSQL.

## 5. Producción

- Cambiar `JPA_DDL_AUTO=validate` y versionar el esquema (Flyway/Liquibase).
- Definir `JWT_SECRET`, `DB_PASSWORD` y las credenciales de Cloudinary como secretos del servidor, nunca en el repositorio.
- Servir detrás de HTTPS (proxy inverso) para que las cookies de sesión viajen cifradas.
- Cambiar o desactivar las cuentas demo.
