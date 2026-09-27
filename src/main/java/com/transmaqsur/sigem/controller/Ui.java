package com.transmaqsur.sigem.controller;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/** Utilidades de presentación disponibles en las vistas como {@code @ui}. */
@Getter
@Component("ui")
public class Ui {

    @Value("${sigem.empresa.nombre}")
    private String empresaNombre;
    @Value("${sigem.empresa.ruc}")
    private String empresaRuc;
    @Value("${sigem.empresa.direccion}")
    private String empresaDireccion;
    @Value("${sigem.empresa.telefono}")
    private String empresaTelefono;
    @Value("${sigem.empresa.email}")
    private String empresaEmail;

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FH = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Map<String, String> COLORES = Map.ofEntries(
            Map.entry("DISPONIBLE", "success"), Map.entry("ACTIVO", "success"), Map.entry("ACTIVA", "success"),
            Map.entry("VIGENTE", "success"), Map.entry("ACEPTADA", "success"), Map.entry("GANADA", "success"),
            Map.entry("COMPLETADA", "success"), Map.entry("PAGADA", "success"), Map.entry("RECIBIDA", "success"),
            Map.entry("APROBADA", "success"), Map.entry("ATENDIDA", "success"), Map.entry("EXITO", "success"),
            Map.entry("ENTRADA", "success"), Map.entry("AJUSTE_POSITIVO", "success"),
            Map.entry("EN_SERVICIO", "primary"), Map.entry("EN_CURSO", "primary"), Map.entry("EN_PROCESO", "primary"),
            Map.entry("ENVIADA", "primary"), Map.entry("EMITIDA", "primary"), Map.entry("COTIZADA", "primary"),
            Map.entry("PROPUESTA", "primary"), Map.entry("INFO", "primary"), Map.entry("CREAR", "success"),
            Map.entry("ACTUALIZAR", "primary"), Map.entry("INICIO_SESION", "info"),
            Map.entry("ASIGNADA", "warning"), Map.entry("PROGRAMADA", "warning"), Map.entry("PROGRAMADO", "warning"),
            Map.entry("PENDIENTE", "warning"), Map.entry("BORRADOR", "secondary"), Map.entry("NEGOCIACION", "warning"),
            Map.entry("REGISTRADA", "warning"), Map.entry("PLANIFICADA", "info"), Map.entry("CALIFICADA", "info"),
            Map.entry("PROSPECTO", "secondary"), Map.entry("ALERTA", "warning"), Map.entry("VACACIONES", "info"),
            Map.entry("LICENCIA", "info"), Map.entry("MEDIA", "info"), Map.entry("ALTA", "warning"), Map.entry("BAJA", "secondary"),
            Map.entry("SALIDA", "danger"), Map.entry("AJUSTE_NEGATIVO", "danger"), Map.entry("PREVENTIVO", "info"),
            Map.entry("CORRECTIVO", "warning"), Map.entry("EN_MANTENIMIENTO", "orange"), Map.entry("RENOVADO", "info"),
            Map.entry("FINALIZADO", "dark"), Map.entry("FINALIZADA", "dark"),
            Map.entry("FUERA_SERVICIO", "danger"), Map.entry("ANULADO", "danger"), Map.entry("ANULADA", "danger"),
            Map.entry("RECHAZADA", "danger"), Map.entry("CANCELADA", "danger"), Map.entry("PERDIDA", "danger"),
            Map.entry("VENCIDA", "danger"), Map.entry("CESADO", "danger"), Map.entry("CRITICA", "danger"),
            Map.entry("PELIGRO", "danger"), Map.entry("ELIMINAR", "danger"), Map.entry("ACCESO_FALLIDO", "danger"));

    private static final Map<String, String> ICONOS = Map.of(
            "INFO", "bi-info-circle", "EXITO", "bi-check-circle", "ALERTA", "bi-exclamation-triangle", "PELIGRO", "bi-exclamation-octagon");

    public record EnlacePagina(String texto, String url, boolean activo, boolean deshabilitado) {
    }

    /** Enlaces de paginación (anterior, números y siguiente) conservando los filtros de la URL actual. */
    public java.util.List<EnlacePagina> enlacesPagina(org.springframework.data.domain.Page<?> pagina) {
        java.util.List<EnlacePagina> enlaces = new java.util.ArrayList<>();
        int actual = pagina.getNumber();
        enlaces.add(new EnlacePagina("Anterior", urlPagina(actual - 1), false, pagina.isFirst()));
        for (int i = Math.max(0, actual - 3); i <= Math.min(pagina.getTotalPages() - 1, actual + 3); i++) {
            enlaces.add(new EnlacePagina(String.valueOf(i + 1), urlPagina(i), i == actual, false));
        }
        enlaces.add(new EnlacePagina("Siguiente", urlPagina(actual + 1), false, pagina.isLast()));
        return enlaces;
    }

    private static String urlPagina(int pagina) {
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().replaceQueryParam("pagina", pagina).build();
        return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
    }

    /** Porcentaje limitado a 100 para barras de progreso. */
    public int tope(int porcentaje) {
        return Math.max(0, Math.min(porcentaje, 100));
    }

    public boolean esNumero(Object valor) {
        return valor instanceof Number;
    }

    /** Texto de una celda de reporte. */
    public String celda(Object valor) {
        if (valor instanceof BigDecimal b) {
            return num(b);
        }
        return valor == null ? "" : valor.toString();
    }

    public String color(Object valor) {
        return valor == null ? "secondary" : COLORES.getOrDefault(valor.toString(), "secondary");
    }

    public String icono(Object tipoNotificacion) {
        return tipoNotificacion == null ? "bi-bell" : ICONOS.getOrDefault(tipoNotificacion.toString(), "bi-bell");
    }

    public String soles(BigDecimal valor) {
        return "S/ " + numero(valor, 2);
    }

    public String num(BigDecimal valor) {
        if (valor == null) {
            return "-";
        }
        BigDecimal v = valor.stripTrailingZeros();
        return numero(v, Math.max(0, Math.min(v.scale(), 2)));
    }

    public String fecha(LocalDate f) {
        return f == null ? "-" : F.format(f);
    }

    public String fecha(LocalDateTime f) {
        return f == null ? "-" : FH.format(f);
    }

    private static String numero(BigDecimal valor, int decimales) {
        BigDecimal v = valor == null ? BigDecimal.ZERO : valor.setScale(decimales, RoundingMode.HALF_UP);
        DecimalFormat df = new DecimalFormat(decimales == 0 ? "#,##0" : "#,##0." + "0".repeat(decimales),
                DecimalFormatSymbols.getInstance(Locale.US));
        return df.format(v);
    }
}
