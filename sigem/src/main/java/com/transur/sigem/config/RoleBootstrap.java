package com.transur.sigem.config;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.compras.entity.Proveedor;
import com.transur.sigem.compras.repository.ProveedorRepository;
import com.transur.sigem.logistica.entity.Almacen;
import com.transur.sigem.logistica.entity.CategoriaRepuesto;
import com.transur.sigem.logistica.entity.InventarioMaquinaria;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.entity.UbicacionAlmacen;
import com.transur.sigem.logistica.repository.AlmacenRepository;
import com.transur.sigem.logistica.repository.CategoriaRepuestoRepository;
import com.transur.sigem.logistica.repository.InventarioMaquinariaRepository;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.Tarifa;
import com.transur.sigem.maquinaria.entity.TipoMaquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
import com.transur.sigem.maquinaria.repository.TipoMaquinariaRepository;
import com.transur.sigem.operaciones.entity.TipoServicio;
import com.transur.sigem.operaciones.repository.TipoServicioRepository;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import com.transur.sigem.security.entity.Permiso;
import com.transur.sigem.security.entity.Rol;
import com.transur.sigem.security.entity.RolPermiso;
import com.transur.sigem.security.entity.RolPermisoId;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.PermisoRepository;
import com.transur.sigem.security.repository.RolPermisoRepository;
import com.transur.sigem.security.repository.RolRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Datos base de SIGEM. Es idempotente: solo crea lo que no existe y nunca
 * sobrescribe contraseñas, clientes o datos operativos creados por el usuario.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class RoleBootstrap implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final RolPermisoRepository rolPermisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final TipoMaquinariaRepository tipoMaquinariaRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final TarifaRepository tarifaRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AlmacenRepository almacenRepository;
    private final CategoriaRepuestoRepository categoriaRepuestoRepository;
    private final RepuestoRepository repuestoRepository;
    private final InventarioMaquinariaRepository inventarioMaquinariaRepository;
    private final com.transur.sigem.logistica.repository.UbicacionAlmacenRepository ubicacionAlmacenRepository;
    private final ProveedorRepository proveedorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Map<String, Rol> roles = crearRoles();
        crearPermisos(roles);
        crearCuentasDemo(roles);
        crearCatalogosBase();
        System.out.println("SIGEM: datos base verificados. Cuentas demo disponibles por rol.");
    }

    private Map<String, Rol> crearRoles() {
        List<String> nombres = List.of("ADMIN", "CLIENTE", "VENTAS", "MARKETING", "OPERACIONES", "RRHH", "LOGISTICA", "GERENCIA");
        Map<String, Rol> roles = new LinkedHashMap<>();
        for (String nombre : nombres) {
            Rol rol = rolRepository.findByNombre(nombre).orElseGet(() -> {
                Rol nuevo = new Rol();
                nuevo.setNombre(nombre);
                nuevo.setDescripcion(descripcion(nombre));
                nuevo.setEstado(true);
                return rolRepository.save(nuevo);
            });
            roles.put(nombre, rol);
        }
        return roles;
    }

    private void crearPermisos(Map<String, Rol> roles) {
        record P(String codigo, String nombre, String modulo, String ruta, String icono, int orden) {}
        List<P> definiciones = List.of(
                new P("VER_DASHBOARD", "Dashboard", "GENERAL", "/inicio", "bi-speedometer2", 1),
                new P("GESTIONAR_USUARIOS", "Usuarios", "ADMIN", "/seguridad/usuarios", "bi-people", 2),
                new P("GESTIONAR_ROLES", "Roles", "ADMIN", "/seguridad/roles", "bi-shield-lock", 3),
                new P("GESTIONAR_CLIENTES", "Clientes", "COMERCIAL", "/marketing/clientes", "bi-person-vcard", 4),
                new P("GESTIONAR_MARKETING", "Marketing", "MARKETING", "/marketing/dashboard", "bi-megaphone", 5),
                new P("GESTIONAR_VENTAS", "Ventas", "VENTAS", "/ventas/dashboard", "bi-cash-stack", 6),
                new P("GESTIONAR_MAQUINARIA", "Maquinaria", "OPERACIONES", "/operaciones/maquinaria", "bi-truck", 7),
                new P("GESTIONAR_SERVICIOS", "Servicios", "OPERACIONES", "/operaciones/servicios", "bi-tools", 8),
                new P("GESTIONAR_RRHH", "RRHH", "RRHH", "/rrhh/dashboard", "bi-person-badge", 9),
                new P("GESTIONAR_LOGISTICA", "Logística", "LOGISTICA", "/logistica/dashboard", "bi-box-seam", 10),
                new P("GESTIONAR_MANTENIMIENTO", "Mantenimiento", "OPERACIONES", "/operaciones/checklist", "bi-wrench-adjustable", 11),
                new P("VER_REPORTES", "Reportes", "GERENCIA", "/gerencia/reportes", "bi-bar-chart-line", 12),
                new P("PORTAL_CLIENTE", "Portal Cliente", "CLIENTE", "/cliente/dashboard", "bi-person-circle", 13)
        );

        for (P d : definiciones) {
            Permiso permiso = permisoRepository.findAll().stream()
                    .filter(p -> d.codigo().equals(p.getCodigo())).findFirst().orElseGet(() -> {
                        Permiso p = new Permiso();
                        p.setCodigo(d.codigo());
                        p.setNombre(d.nombre());
                        p.setModulo(d.modulo());
                        p.setRuta(d.ruta());
                        p.setIcono(d.icono());
                        p.setOrden(d.orden());
                        p.setEstado(true);
                        return permisoRepository.save(p);
                    });

            for (Rol rol : roles.values()) {
                boolean asignar = "ADMIN".equals(rol.getNombre()) ||
                        ("CLIENTE".equals(rol.getNombre()) && "PORTAL_CLIENTE".equals(permiso.getCodigo())) ||
                        ("VENTAS".equals(rol.getNombre()) && List.of("VER_DASHBOARD", "GESTIONAR_CLIENTES", "GESTIONAR_VENTAS").contains(permiso.getCodigo())) ||
                        ("MARKETING".equals(rol.getNombre()) && List.of("VER_DASHBOARD", "GESTIONAR_CLIENTES", "GESTIONAR_MARKETING").contains(permiso.getCodigo())) ||
                        ("OPERACIONES".equals(rol.getNombre()) && List.of("VER_DASHBOARD", "GESTIONAR_MAQUINARIA", "GESTIONAR_SERVICIOS", "GESTIONAR_MANTENIMIENTO").contains(permiso.getCodigo())) ||
                        ("RRHH".equals(rol.getNombre()) && List.of("VER_DASHBOARD", "GESTIONAR_RRHH").contains(permiso.getCodigo())) ||
                        ("LOGISTICA".equals(rol.getNombre()) && List.of("VER_DASHBOARD", "GESTIONAR_LOGISTICA").contains(permiso.getCodigo())) ||
                        ("GERENCIA".equals(rol.getNombre()) && List.of("VER_DASHBOARD", "VER_REPORTES").contains(permiso.getCodigo()));
                if (asignar && !rolPermisoRepository.existsById(new RolPermisoId(rol.getId(), permiso.getId()))) {
                    rolPermisoRepository.save(new RolPermiso(new RolPermisoId(rol.getId(), permiso.getId()), rol, permiso));
                }
            }
        }
    }

    private void crearCuentasDemo(Map<String, Rol> roles) {
        crearUsuario("admin", "Admin123*", "admin@transur.com", "Administrador", "SIGEM", roles.get("ADMIN"));
        crearUsuario("gerente", "Gerente123*", "gerente@transur.com", "Gerente", "General", roles.get("GERENCIA"));
        crearUsuario("ventas", "Ventas123*", "ventas@transur.com", "Usuario", "Ventas", roles.get("VENTAS"));
        crearUsuario("marketing", "Marketing123*", "marketing@transur.com", "Usuario", "Marketing", roles.get("MARKETING"));
        crearUsuario("operaciones", "Operaciones123*", "operaciones@transur.com", "Usuario", "Operaciones", roles.get("OPERACIONES"));
        crearUsuario("rrhh", "Rrhh123*", "rrhh@transur.com", "Usuario", "RRHH", roles.get("RRHH"));
        crearUsuario("logistica", "Logistica123*", "logistica@transur.com", "Usuario", "Logística", roles.get("LOGISTICA"));

        Cliente cliente = clienteRepository.findByDocumento("20600000001").orElseGet(() -> {
            Cliente c = new Cliente();
            c.setTipoCliente("JURIDICA");
            c.setRazonSocial("Constructora Demo SAC");
            c.setDocumento("20600000001");
            c.setTelefono("999111222");
            c.setEmail("cliente@transur.com");
            c.setDireccion("Av. Demo 123, Lima");
            c.setEstado(true);
            c.setFechaRegistro(LocalDateTime.now());
            return clienteRepository.save(c);
        });

        Usuario usuarioCliente = usuarioRepository.findByUsername("cliente").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("cliente");
            u.setPassword(passwordEncoder.encode("Cliente123*"));
            u.setEmail("cliente@transur.com");
            u.setNombres("Cliente");
            u.setApellidos("Demo");
            u.setRol(roles.get("CLIENTE"));
            u.setEstado(true);
            u.setFechaCreacion(LocalDateTime.now());
            return usuarioRepository.save(u);
        });
        usuarioCliente.setCliente(cliente);
        usuarioRepository.save(usuarioCliente);
        cliente.setUsuario(usuarioCliente);
        clienteRepository.save(cliente);
    }

    private Usuario crearUsuario(String username, String password, String email, String nombres, String apellidos, Rol rol) {
        Usuario existente = usuarioRepository.findByUsername(username).orElse(null);
        if (existente != null) {
            if (existente.getRol() == null) existente.setRol(rol);
            if (existente.getEstado() == null) existente.setEstado(true);
            return usuarioRepository.save(existente);
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setEmail(email);
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setRol(rol);
        u.setEstado(true);
        u.setFechaCreacion(LocalDateTime.now());
        return usuarioRepository.save(u);
    }

    private void crearCatalogosBase() {
        TipoMaquinaria excavadora = tipoMaquinariaRepository.findByNombre("EXCAVADORA").orElseGet(() -> {
            TipoMaquinaria t = new TipoMaquinaria();
            t.setNombre("EXCAVADORA");
            t.setDescripcion("Excavadoras hidráulicas para movimiento de tierras.");
            return tipoMaquinariaRepository.save(t);
        });
        TipoMaquinaria retro = tipoMaquinariaRepository.findByNombre("RETROEXCAVADORA").orElseGet(() -> {
            TipoMaquinaria t = new TipoMaquinaria();
            t.setNombre("RETROEXCAVADORA");
            t.setDescripcion("Retroexcavadoras para trabajos generales.");
            return tipoMaquinariaRepository.save(t);
        });
        tipoMaquinariaRepository.findByNombre("VOLQUETE").orElseGet(() -> {
            TipoMaquinaria t = new TipoMaquinaria();
            t.setNombre("VOLQUETE");
            t.setDescripcion("Volquetes para transporte de material.");
            return tipoMaquinariaRepository.save(t);
        });

        Maquinaria maquina = maquinariaRepository.findByCodigoInterno("EXC-DEMO-001").orElseGet(() -> {
            Maquinaria m = new Maquinaria();
            m.setCodigoInterno("EXC-DEMO-001");
            m.setTipo(excavadora);
            m.setMarca("CAT");
            m.setModelo("320D");
            m.setAnio(2024);
            m.setPlaca("A0D-001");
            m.setHorometroActual(BigDecimal.valueOf(1250));
            m.setEstadoOperativo("OPERATIVO");
            m.setDestinoOperativo("MULTIPROPOSITO");
            m.setPrecioBaseAlquiler(BigDecimal.valueOf(1200));
            m.setPrecioBaseVenta(BigDecimal.valueOf(480000));
            return maquinariaRepository.save(m);
        });
        crearTarifaSiFalta(maquina, "HORA", BigDecimal.valueOf(180));
        crearTarifaSiFalta(maquina, "DIA", BigDecimal.valueOf(1200));
        crearTarifaSiFalta(maquina, "SEMANA", BigDecimal.valueOf(6500));
        crearTarifaSiFalta(maquina, "MES", BigDecimal.valueOf(22000));

        if (maquinariaRepository.findByCodigoInterno("RET-DEMO-001").isEmpty()) {
            Maquinaria m = new Maquinaria();
            m.setCodigoInterno("RET-DEMO-001");
            m.setTipo(retro);
            m.setMarca("JCB");
            m.setModelo("3CX");
            m.setAnio(2023);
            m.setPlaca("A0D-002");
            m.setHorometroActual(BigDecimal.valueOf(980));
            m.setEstadoOperativo("OPERATIVO");
            m.setDestinoOperativo("MULTIPROPOSITO");
            m.setPrecioBaseAlquiler(BigDecimal.valueOf(850));
            m.setPrecioBaseVenta(BigDecimal.valueOf(280000));
            maquinariaRepository.save(m);
        }

        crearTipoServicio("MOVIMIENTO_TIERRA", "Movimiento y nivelación de tierras.");
        crearTipoServicio("TRANSPORTE_CARGA", "Transporte de materiales y carga.");
        crearTipoServicio("GRUA", "Izaje y manipulación de cargas.");
        crearTipoServicio("COMPACTACION", "Compactación de terrenos.");

        if (empleadoRepository.findByDocumento("70000001").isEmpty()) {
            Empleado e = new Empleado();
            e.setDocumento("70000001");
            e.setNombres("Carlos");
            e.setApellidos("Operador Demo");
            e.setCargo("OPERADOR");
            e.setTelefono("999333444");
            e.setEmail("operador@transur.com");
            e.setFechaIngreso(LocalDate.now().minusYears(1));
            e.setEstado(true);
            empleadoRepository.save(e);
        }

        CategoriaRepuesto filtros = categoriaRepuestoRepository.findByNombre("FILTROS").orElseGet(() -> {
            CategoriaRepuesto c = new CategoriaRepuesto();
            c.setNombre("FILTROS");
            c.setDescripcion("Filtros de aire, aceite y combustible.");
            return categoriaRepuestoRepository.save(c);
        });
        if (repuestoRepository.findByCodigo("FIL-DEMO-001").isEmpty()) {
            Repuesto r = new Repuesto();
            r.setCodigo("FIL-DEMO-001");
            r.setNombre("Filtro de aceite hidráulico");
            r.setDescripcion("Repuesto de demostración para pruebas de inventario.");
            r.setCategoria(filtros);
            r.setStockActual(10);
            r.setStockMinimo(3);
            r.setUnidadMedida("UNIDAD");
            r.setEstado(true);
            repuestoRepository.save(r);
        }

        Almacen almacen = almacenRepository.findByNombre("ALMACEN CENTRAL").orElseGet(() -> {
            Almacen a = new Almacen();
            a.setNombre("ALMACEN CENTRAL");
            a.setDescripcion("Almacén principal de repuestos y suministros.");
            a.setDireccion("Patio Central TRANSUR, Lima");
            a.setEstado(true);
            return almacenRepository.save(a);
        });
        if (ubicacionAlmacenRepository.findByCodigoAndAlmacenId("A-01", almacen.getId()).isEmpty()) {
            UbicacionAlmacen u = new UbicacionAlmacen();
            u.setAlmacen(almacen);
            u.setCodigo("A-01");
            u.setNombre("Estantería A-01");
            u.setEstado(true);
            ubicacionAlmacenRepository.save(u);
        }
        if (inventarioMaquinariaRepository.findByMaquinariaId(maquina.getId()).isEmpty()) {
            InventarioMaquinaria im = new InventarioMaquinaria();
            im.setMaquinaria(maquina);
            im.setSituacionLogistica("EN_PATIO_CENTRAL");
            im.setFechaIngreso(LocalDate.now().minusMonths(2));
            im.setObservaciones("Registro inicial de demostración.");
            inventarioMaquinariaRepository.save(im);
        }

        if (proveedorRepository.findByDocumento("20699999999").isEmpty()) {
            Proveedor p = new Proveedor();
            p.setTipoProveedor("JURIDICA");
            p.setRazonSocial("Repuestos Andinos SAC");
            p.setDocumento("20699999999");
            p.setTelefono("999555666");
            p.setEmail("ventas@repuestosandinos.com");
            p.setDireccion("Av. Industrial 500, Lima");
            p.setEstado(true);
            p.setFechaRegistro(LocalDateTime.now());
            proveedorRepository.save(p);
        }
    }

    private void crearTarifaSiFalta(Maquinaria maquina, String tipo, BigDecimal precio) {
        boolean existe = tarifaRepository.findByMaquinariaId(maquina.getId()).stream().anyMatch(t -> tipo.equals(t.getTipoTarifa()) && t.getVigenciaFin() == null);
        if (!existe) {
            Tarifa t = new Tarifa();
            t.setMaquinaria(maquina);
            t.setTipoTarifa(tipo);
            t.setPrecio(precio);
            t.setVigenciaInicio(LocalDate.now());
            tarifaRepository.save(t);
        }
    }

    private void crearTipoServicio(String nombre, String descripcion) {
        if (tipoServicioRepository.findByNombre(nombre).isEmpty()) {
            TipoServicio t = new TipoServicio();
            t.setNombre(nombre);
            t.setDescripcion(descripcion);
            t.setEstado(true);
            tipoServicioRepository.save(t);
        }
    }

    private String descripcion(String nombre) {
        return switch (nombre) {
            case "ADMIN" -> "Administración y seguridad del sistema";
            case "CLIENTE" -> "Portal de clientes";
            case "VENTAS" -> "Gestión comercial";
            case "MARKETING" -> "Marketing y oportunidades";
            case "OPERACIONES" -> "Gestión de flota y servicios";
            case "RRHH" -> "Recursos Humanos";
            case "LOGISTICA" -> "Logística, almacén y compras";
            case "GERENCIA" -> "Gerencia General y análisis ejecutivo";
            default -> "Rol SIGEM";
        };
    }
}
