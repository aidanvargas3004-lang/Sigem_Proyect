package com.transur.sigem.sistema.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.common.util.UsuarioActual;
import com.transur.sigem.sistema.dao.AuditoriaDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Respaldo (backup) de la base de datos PostgreSQL.
 * <p>
 * Usa pg_dump (copia completa: estructura + datos) cuando está disponible; si no se encuentra,
 * genera una exportación de datos por JDBC para que el respaldo nunca deje de hacerse.
 * Se ejecuta a diario de forma programada y también a pedido desde /seguridad/respaldos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RespaldoService {

    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^sigem_db_\\d{8}_\\d{6}\\.sql$");
    private static final Pattern URL_PG = Pattern.compile("^jdbc:postgresql://([^:/]+)(?::(\\d+))?/([^?]+).*$");
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final DataSourceProperties dataSource;
    private final JdbcTemplate jdbc;
    private final AuditoriaDao auditoriaDao;

    @Value("${sigem.backup.dir:backups}")
    private String directorio;

    @Value("${sigem.backup.pg-dump:}")
    private String pgDumpConfigurado;

    /** auto = pg_dump si existe, si no JDBC; jdbc = siempre exportación JDBC. */
    @Value("${sigem.backup.metodo:auto}")
    private String metodoPreferido;

    @Value("${sigem.backup.retener:15}")
    private int retener;

    /** Respaldo automático diario (por defecto a las 02:00). */
    @Scheduled(cron = "${sigem.backup.cron:0 0 2 * * *}")
    public void respaldoProgramado() {
        try {
            RespaldoInfo r = generar("PROGRAMADO");
            log.info("Respaldo programado generado: {} ({})", r.nombre(), r.getTamano());
        } catch (RuntimeException e) {
            log.error("Falló el respaldo programado: {}", e.getMessage());
        }
    }

    /** Genera un respaldo y devuelve sus datos. */
    public RespaldoInfo generar(String origen) {
        Path dir = carpeta();
        Path archivo = dir.resolve("sigem_db_" + LocalDateTime.now().format(SELLO) + ".sql");
        String metodo;
        Optional<String> pgDump = buscarPgDump();
        try {
            if (pgDump.isPresent()) {
                ejecutarPgDump(pgDump.get(), archivo);
                metodo = "pg_dump";
            } else {
                exportarPorJdbc(archivo);
                metodo = "JDBC";
            }
        } catch (IOException e) {
            throw new NegocioException("No se pudo escribir el respaldo: " + e.getMessage());
        }
        purgarAntiguos(dir);
        RespaldoInfo info = info(archivo);
        registrarAuditoria(info, metodo, origen);
        return info;
    }

    public List<RespaldoInfo> listar() {
        try (Stream<Path> s = Files.list(carpeta())) {
            return s.filter(p -> NOMBRE_VALIDO.matcher(p.getFileName().toString()).matches())
                    .map(this::info)
                    .sorted(Comparator.comparing(RespaldoInfo::nombre).reversed())
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /** Ruta de un respaldo existente; valida el nombre para impedir recorrer otras carpetas. */
    public Path archivo(String nombre) {
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new RecursoNoEncontradoException("Respaldo", nombre);
        }
        Path p = carpeta().resolve(nombre);
        if (!Files.exists(p)) {
            throw new RecursoNoEncontradoException("Respaldo", nombre);
        }
        return p;
    }

    /** Datos de conexión visibles en la pantalla de configuración (sin la contraseña). */
    public Map<String, String> configuracion() {
        Map<String, String> c = new LinkedHashMap<>();
        Matcher m = URL_PG.matcher(dataSource.getUrl());
        c.put("Motor", "PostgreSQL " + jdbc.queryForObject("SHOW server_version", String.class).split(" ")[0]);
        c.put("URL JDBC", dataSource.getUrl());
        if (m.matches()) {
            c.put("Servidor", m.group(1));
            c.put("Puerto", m.group(2) != null ? m.group(2) : "5432");
            c.put("Base de datos", m.group(3));
        }
        c.put("Usuario", dataSource.getUsername());
        c.put("Tamaño de la base", jdbc.queryForObject("SELECT pg_size_pretty(pg_database_size(current_database()))", String.class));
        c.put("Tablas", String.valueOf(jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'", Long.class)));
        c.put("Carpeta de respaldos", carpeta().toAbsolutePath().toString());
        c.put("Herramienta", buscarPgDump().map(p -> "pg_dump (" + p + ")").orElse("jdbc".equalsIgnoreCase(metodoPreferido)
                ? "Exportación JDBC (configurada)" : "Exportación JDBC (pg_dump no encontrado)"));
        c.put("Respaldo automático", "Diario, se conservan los últimos " + retener);
        return c;
    }

    // ------------------------------------------------------------------ internos

    private void ejecutarPgDump(String pgDump, Path archivo) throws IOException {
        Matcher m = URL_PG.matcher(dataSource.getUrl());
        if (!m.matches()) {
            throw new NegocioException("URL de base de datos no reconocida: " + dataSource.getUrl());
        }
        ProcessBuilder pb = new ProcessBuilder(pgDump,
                "-h", m.group(1), "-p", m.group(2) != null ? m.group(2) : "5432",
                "-U", dataSource.getUsername(), "-d", m.group(3),
                "--no-owner", "--no-privileges", "--encoding=UTF8", "-f", archivo.toString());
        pb.environment().put("PGPASSWORD", Objects.toString(dataSource.getPassword(), ""));
        pb.redirectErrorStream(true);
        Process proceso = pb.start();
        String salida = new String(proceso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try {
            if (!proceso.waitFor(5, TimeUnit.MINUTES) || proceso.exitValue() != 0) {
                Files.deleteIfExists(archivo);
                throw new NegocioException("pg_dump terminó con error: " + salida.trim());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NegocioException("Respaldo interrumpido");
        }
    }

    /** Exportación de datos sin herramientas externas: INSERT por tabla y ajuste de secuencias. */
    private void exportarPorJdbc(Path archivo) throws IOException {
        List<String> tablas = jdbc.queryForList("""
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name""", String.class);
        try (BufferedWriter w = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            w.write("-- Respaldo de datos SIGEM (exportación JDBC) " + LocalDateTime.now() + "\n");
            w.write("-- Restaurar sobre una base con la estructura creada (la aplicación la crea al iniciar).\n");
            w.write("SET session_replication_role = replica;\nBEGIN;\n");
            for (String t : tablas) {
                w.write("\n-- Tabla " + t + "\nDELETE FROM public." + t + ";\n");
                jdbc.query("SELECT * FROM public." + t, rs -> {
                    ResultSetMetaData md = rs.getMetaData();
                    StringBuilder cols = new StringBuilder();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        cols.append(i > 1 ? ", " : "").append(md.getColumnName(i));
                    }
                    StringBuilder sb = new StringBuilder("INSERT INTO public.").append(t).append(" (").append(cols).append(") VALUES (");
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        Object v = rs.getObject(i);
                        sb.append(i > 1 ? ", " : "");
                        if (v == null) sb.append("NULL");
                        else if (v instanceof Number || v instanceof Boolean) sb.append(v);
                        else sb.append('\'').append(v.toString().replace("'", "''")).append('\'');
                    }
                    try {
                        w.write(sb.append(");\n").toString());
                    } catch (IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                });
            }
            w.write("\n-- Secuencias\n");
            for (Map<String, Object> s : jdbc.queryForList("""
                    SELECT c.relname AS tabla, a.attname AS columna
                    FROM pg_class c JOIN pg_attribute a ON a.attrelid = c.oid
                    JOIN pg_namespace n ON n.oid = c.relnamespace
                    WHERE n.nspname = 'public' AND c.relkind = 'r'
                      AND pg_get_serial_sequence('public.' || c.relname, a.attname) IS NOT NULL""")) {
                w.write("SELECT setval(pg_get_serial_sequence('public." + s.get("tabla") + "', '" + s.get("columna")
                        + "'), COALESCE((SELECT MAX(" + s.get("columna") + ") FROM public." + s.get("tabla") + "), 1));\n");
            }
            w.write("COMMIT;\nSET session_replication_role = DEFAULT;\n");
        }
    }

    Optional<String> buscarPgDump() {
        if ("jdbc".equalsIgnoreCase(metodoPreferido)) return Optional.empty();
        List<String> candidatos = new ArrayList<>();
        if (pgDumpConfigurado != null && !pgDumpConfigurado.isBlank()) candidatos.add(pgDumpConfigurado);
        candidatos.add("pg_dump");
        for (int v = 18; v >= 12; v--) {
            candidatos.add("C:\\Program Files\\PostgreSQL\\" + v + "\\bin\\pg_dump.exe");
            candidatos.add("/usr/lib/postgresql/" + v + "/bin/pg_dump");
        }
        for (String c : candidatos) {
            if (c.contains("/") || c.contains("\\")) {
                if (Files.isExecutable(Paths.get(c))) return Optional.of(c);
            } else if (responde(c)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    private static boolean responde(String comando) {
        try {
            Process p = new ProcessBuilder(comando, "--version").redirectErrorStream(true).start();
            p.getInputStream().readAllBytes();
            return p.waitFor(10, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private Path carpeta() {
        try {
            return Files.createDirectories(Paths.get(directorio));
        } catch (IOException e) {
            throw new NegocioException("No se pudo crear la carpeta de respaldos: " + directorio);
        }
    }

    private void purgarAntiguos(Path dir) {
        List<RespaldoInfo> todos = listar();
        for (int i = retener; i < todos.size(); i++) {
            try {
                Files.deleteIfExists(dir.resolve(todos.get(i).nombre()));
            } catch (IOException e) {
                log.warn("No se pudo eliminar el respaldo antiguo {}", todos.get(i).nombre());
            }
        }
    }

    private RespaldoInfo info(Path p) {
        try {
            return new RespaldoInfo(p.getFileName().toString(), Files.size(p),
                    LocalDateTime.ofInstant(Files.getLastModifiedTime(p).toInstant(), ZoneId.systemDefault()));
        } catch (IOException e) {
            return new RespaldoInfo(p.getFileName().toString(), 0, LocalDateTime.now());
        }
    }

    private void registrarAuditoria(RespaldoInfo info, String metodo, String origen) {
        try {
            String json = new ObjectMapper().writeValueAsString(Map.of(
                    "archivo", info.nombre(), "tamano", info.getTamano(), "metodo", metodo, "origen", origen));
            auditoriaDao.insertar(UsuarioActual.username().orElse(null), "SISTEMA", "RESPALDO", "BaseDatos", null, null, json);
        } catch (Exception e) {
            log.warn("No se pudo auditar el respaldo: {}", e.getMessage());
        }
    }
}
