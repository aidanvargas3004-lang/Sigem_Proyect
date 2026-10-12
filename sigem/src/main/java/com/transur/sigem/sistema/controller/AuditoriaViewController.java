package com.transur.sigem.sistema.controller;

import com.transur.sigem.common.reporte.ExportadorReporteFactory;
import com.transur.sigem.common.reporte.TablaReporte;
import com.transur.sigem.sistema.dao.AuditoriaDao;
import com.transur.sigem.sistema.dao.FiltroAuditoria;
import com.transur.sigem.sistema.dao.RegistroAuditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Consulta de la bitácora de auditoría (RF018). Acceso restringido a ADMIN por SecurityConfig. */
@Controller
@RequestMapping("/seguridad/auditoria")
@RequiredArgsConstructor
public class AuditoriaViewController {

    private static final List<String> ACCIONES = List.of("CREAR", "ACTUALIZAR", "ELIMINAR", "LOGIN", "LOGIN_FALLIDO");

    private final AuditoriaDao auditoriaDao;
    private final ExportadorReporteFactory exportadores;

    @GetMapping
    public String bitacora(@RequestParam(required = false) String modulo,
                           @RequestParam(required = false) String accion,
                           @RequestParam(required = false) String usuario,
                           @RequestParam(required = false) String entidad,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                           Model model) {
        FiltroAuditoria filtro = new FiltroAuditoria(modulo, accion, usuario, entidad, desde, hasta, 300);
        model.addAttribute("registros", auditoriaDao.buscar(filtro));
        model.addAttribute("filtro", filtro);
        model.addAttribute("modulos", auditoriaDao.modulos());
        model.addAttribute("acciones", ACCIONES);
        model.addAttribute("resumen", auditoriaDao.resumenPorModulo(7));
        return "seguridad/auditoria";
    }

    @GetMapping("/exportar/{formato}")
    public ResponseEntity<byte[]> exportar(@PathVariable String formato,
                                           @RequestParam(required = false) String modulo,
                                           @RequestParam(required = false) String accion,
                                           @RequestParam(required = false) String usuario,
                                           @RequestParam(required = false) String entidad,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<RegistroAuditoria> registros = auditoriaDao.buscar(new FiltroAuditoria(modulo, accion, usuario, entidad, desde, hasta, 5000));
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        List<List<Object>> filas = new ArrayList<>();
        for (RegistroAuditoria r : registros) {
            filas.add(List.of(f.format(r.fecha()), r.usuario(), r.modulo(), r.accion(), nvl(r.entidad()),
                    r.entidadId() == null ? "" : r.entidadId(), nvl(r.datosAnteriores()), nvl(r.datosNuevos())));
        }
        TablaReporte tabla = new TablaReporte("Bitácora de auditoría", "Registros: " + registros.size(),
                List.of("Fecha", "Usuario", "Módulo", "Acción", "Entidad", "ID", "Datos anteriores", "Datos nuevos"), filas);
        return exportadores.descargar(formato, "auditoria", tabla);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
