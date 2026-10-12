package com.transur.sigem.operaciones.service;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.reporte.TablaReporte;
import com.transur.sigem.operaciones.dao.ReporteOperativoDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma los reportes operativos (RF016) en un modelo neutro {@link TablaReporte}
 * que la vista muestra y los exportadores convierten a Excel o PDF (RF017).
 */
@Service
@RequiredArgsConstructor
public class ReporteOperativoService {

    /** Tipo de reporte → título visible. */
    public static final Map<String, String> TIPOS = new LinkedHashMap<>();
    static {
        TIPOS.put("servicios", "Servicios realizados");
        TIPOS.put("flota", "Uso de flota por unidad");
        TIPOS.put("mantenimientos", "Mantenimientos");
        TIPOS.put("incidencias", "Incidencias");
        TIPOS.put("disponibilidad", "Disponibilidad de flota");
    }

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReporteOperativoDao dao;

    public TablaReporte generar(String tipo, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        String titulo = TIPOS.get(tipo);
        if (titulo == null) throw new NegocioException("Tipo de reporte no válido: " + tipo);
        String periodo = "Período: " + desde.format(F) + " al " + hasta.format(F);
        return switch (tipo) {
            case "servicios" -> tabla(titulo, periodo,
                    List.of("N°", "Programado", "Inicio real", "Fin real", "Cliente", "Tipo", "Origen", "Destino", "Unidades", "Operadores", "Horas", "Km", "Estado"),
                    dao.serviciosRealizados(desde, hasta),
                    List.of("id", "fecha_programada", "fecha_inicio_real", "fecha_fin_real", "cliente", "tipo_servicio", "origen", "destino", "unidades", "operadores", "horas_uso", "km_recorridos", "estado"));
            case "flota" -> tabla(titulo, periodo,
                    List.of("Unidad", "Tipo", "Placa", "Estado", "Servicios", "Horas uso", "Km", "Mantenimientos", "Costo mant. (S/)", "Incidencias", "Horómetro", "Kilometraje"),
                    dao.usoDeFlota(desde, hasta),
                    List.of("codigo_interno", "tipo", "placa", "estado_operativo", "servicios", "horas_uso", "km_recorridos", "mantenimientos", "costo_mantenimiento", "incidencias", "horometro_actual", "kilometraje_actual"));
            case "mantenimientos" -> tabla(titulo, periodo,
                    List.of("OT", "Unidad", "Tipo", "Estado", "Programado", "Inicio", "Fin", "Técnico", "Plan preventivo", "Repuestos", "Costo (S/)"),
                    dao.mantenimientos(desde, hasta),
                    List.of("id", "codigo_interno", "tipo_mantenimiento", "estado", "fecha_programada", "fecha_inicio", "fecha_fin", "tecnico", "plan", "repuestos", "costo"));
            case "incidencias" -> tabla(titulo, periodo,
                    List.of("N°", "Fecha", "Servicio", "Unidad", "Tipo", "Estado", "Descripción"),
                    dao.incidencias(desde, hasta),
                    List.of("id", "fecha", "servicio_id", "unidad", "tipo", "estado", "descripcion"));
            default -> tabla(titulo, "Situación actual de la flota",
                    List.of("Estado operativo", "Unidades", "% de la flota", "Códigos"),
                    dao.disponibilidad(),
                    List.of("estado_operativo", "unidades", "porcentaje", "codigos"));
        };
    }

    public Map<String, Object> indicadores(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        return dao.indicadores(desde, hasta);
    }

    static void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) throw new NegocioException("Indique el período del reporte");
        if (hasta.isBefore(desde)) throw new NegocioException("La fecha final no puede ser anterior a la inicial");
        if (desde.plusYears(2).isBefore(hasta)) throw new NegocioException("El período máximo es de 2 años");
    }

    private static TablaReporte tabla(String titulo, String subtitulo, List<String> columnas,
                                      List<Map<String, Object>> datos, List<String> claves) {
        List<List<Object>> filas = new ArrayList<>();
        for (Map<String, Object> d : datos) {
            List<Object> fila = new ArrayList<>(claves.size());
            for (String k : claves) fila.add(formato(d.get(k)));
            filas.add(fila);
        }
        return new TablaReporte(titulo, subtitulo, columnas, filas);
    }

    static Object formato(Object v) {
        if (v == null) return "";
        if (v instanceof Timestamp t) return t.toLocalDateTime().format(FH);
        if (v instanceof Date d) return d.toLocalDate().format(F);
        if (v instanceof BigDecimal b) return b.setScale(2, RoundingMode.HALF_UP);
        return v;
    }
}
