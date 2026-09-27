package com.transmaqsur.sigem.config;

import com.transmaqsur.sigem.model.Rol;
import com.transmaqsur.sigem.model.Usuario;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.repository.RolRepository;
import com.transmaqsur.sigem.repository.UsuarioRepository;
import com.transmaqsur.sigem.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.transmaqsur.sigem.model.enums.Modulo.*;

/**
 * Crea los roles de TRANSMAQ SUR con sus permisos y el usuario administrador
 * la primera vez que se inicia el sistema.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DatosIniciales implements ApplicationRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (rolRepository.count() > 0) {
            return;
        }
        log.info("Creando roles y usuario administrador...");
        Map<String, Object[]> roles = new LinkedHashMap<>();
        roles.put(UsuarioService.ROL_ADMIN, new Object[]{"Acceso total al sistema", null, null});
        roles.put("GERENCIA", new Object[]{"Consulta de toda la información, reportes y auditoría",
                Modulo.values(), new Modulo[]{REPORTES}});
        roles.put("JEFE_OPERACIONES", new Object[]{"Planifica rutas, programa servicios y asigna unidades",
                new Modulo[]{CLIENTES, CONTRATOS, SOLICITUDES, EMPLEADOS, MANTENIMIENTO, REPORTES, ALMACENES},
                new Modulo[]{FLOTA, ALQUILERES, SERVICIOS, ASIGNACIONES, GPS}});
        roles.put("JEFE_MANTENIMIENTO", new Object[]{"Gestiona mantenimientos preventivos y correctivos",
                new Modulo[]{COMPRAS, EMPLEADOS, ASIGNACIONES, ALQUILERES, SERVICIOS, REPORTES, GPS},
                new Modulo[]{MANTENIMIENTO, FLOTA, ALMACENES}});
        roles.put("TECNICO", new Object[]{"Ejecuta órdenes de mantenimiento",
                new Modulo[]{FLOTA, ALMACENES}, new Modulo[]{MANTENIMIENTO}});
        roles.put("ASISTENTE_ADMINISTRATIVO", new Object[]{"Compras, almacén, contratos y facturación",
                new Modulo[]{CLIENTES, ALQUILERES, SERVICIOS, FLOTA, REPORTES, SOLICITUDES},
                new Modulo[]{VENTAS, COMPRAS, ALMACENES, CONTRATOS}});
        roles.put("COORDINADOR_COMERCIAL", new Object[]{"Atiende solicitudes, cotiza y renueva contratos",
                new Modulo[]{VENTAS, FLOTA, ASIGNACIONES, ALQUILERES, SERVICIOS, REPORTES},
                new Modulo[]{CLIENTES, MARKETING, SOLICITUDES, CONTRATOS}});
        roles.put("RECURSOS_HUMANOS", new Object[]{"Gestiona conductores, operadores y técnicos",
                new Modulo[]{ASIGNACIONES, REPORTES}, new Modulo[]{EMPLEADOS}});
        roles.put("CONDUCTOR_OPERADOR", new Object[]{"Consulta sus asignaciones y reporta su ubicación",
                new Modulo[]{FLOTA}, new Modulo[]{GPS}});

        for (Map.Entry<String, Object[]> e : roles.entrySet()) {
            Rol rol = new Rol();
            rol.setNombre(e.getKey());
            rol.setDescripcion((String) e.getValue()[0]);
            if (e.getKey().equals(UsuarioService.ROL_ADMIN)) {
                rol.setPermisos(UsuarioService.todosLosPermisos());
            } else {
                Set<String> permisos = new HashSet<>();
                for (Modulo m : (Modulo[]) e.getValue()[1]) {
                    permisos.add(m.getPermisoVer());
                }
                for (Modulo m : (Modulo[]) e.getValue()[2]) {
                    permisos.add(m.getPermisoVer());
                    permisos.add(m.getPermisoGestionar());
                }
                if (e.getKey().equals("GERENCIA")) {
                    permisos.remove(USUARIOS.getPermisoVer());
                }
                rol.setPermisos(permisos);
            }
            rolRepository.save(rol);
        }

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNombreCompleto("Administrador del sistema");
        admin.setEmail("sistemas@transmaqsur.pe");
        admin.setRol(rolRepository.findByNombre(UsuarioService.ROL_ADMIN).orElseThrow());
        usuarioRepository.save(admin);
        log.info("Usuario administrador creado: admin / admin123 (cámbiela después del primer ingreso)");
    }
}
