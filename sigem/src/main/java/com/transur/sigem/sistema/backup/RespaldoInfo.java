package com.transur.sigem.sistema.backup;

import java.time.LocalDateTime;

/** Datos de un archivo de respaldo para listarlo en pantalla. */
public record RespaldoInfo(String nombre, long bytes, LocalDateTime fecha) {

    public String getTamano() {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
    }
}
