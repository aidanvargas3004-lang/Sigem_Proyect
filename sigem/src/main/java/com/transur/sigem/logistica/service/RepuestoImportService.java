package com.transur.sigem.logistica.service;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.util.UsuarioActual;
import com.transur.sigem.logistica.dto.MovimientoRequest;
import com.transur.sigem.logistica.dto.ResultadoImportacion;
import com.transur.sigem.logistica.entity.CategoriaRepuesto;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.CategoriaRepuestoRepository;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

/**
 * Importación de ficheros: carga masiva del catálogo de repuestos desde una hoja Excel (.xlsx).
 * Los repuestos nuevos ingresan su stock inicial como movimiento ENTRADA (queda en el kardex);
 * en los existentes solo se actualizan los datos del catálogo, el stock se mueve con movimientos.
 */
@Service
@RequiredArgsConstructor
public class RepuestoImportService {

    public static final List<String> COLUMNAS =
            List.of("codigo", "nombre", "categoria", "unidad_medida", "stock_actual", "stock_minimo", "descripcion");
    static final int MAX_FILAS = 2000;

    private final RepuestoRepository repuestoRepository;
    private final CategoriaRepuestoRepository categoriaRepository;
    private final MovimientoInventarioService movimientoService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ResultadoImportacion importar(String nombreArchivo, InputStream contenido) {
        if (nombreArchivo == null || !nombreArchivo.toLowerCase().endsWith(".xlsx")) {
            throw new NegocioException("Seleccione un archivo Excel (.xlsx) con la plantilla de repuestos");
        }
        int creados = 0, actualizados = 0;
        List<String> errores = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(contenido)) {
            Sheet hoja = wb.getSheetAt(0);
            Map<String, Integer> col = columnas(hoja.getRow(0));
            if (hoja.getLastRowNum() > MAX_FILAS) {
                throw new NegocioException("El archivo supera el máximo de " + MAX_FILAS + " filas");
            }
            DataFormatter fmt = new DataFormatter();
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                String codigo = texto(fila, col, "codigo", fmt);
                if (codigo.isEmpty()) continue; // fila vacía
                try {
                    boolean nuevo = procesarFila(fila, col, fmt, codigo.toUpperCase());
                    if (nuevo) creados++; else actualizados++;
                } catch (NegocioException e) {
                    errores.add("Fila " + (i + 1) + " (" + codigo + "): " + e.getMessage());
                }
            }
        } catch (IOException | org.apache.poi.EmptyFileException | org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException e) {
            throw new NegocioException("No se pudo leer el archivo Excel: " + e.getMessage());
        }
        return new ResultadoImportacion(creados, actualizados, errores);
    }

    /** Plantilla de importación con encabezados y dos filas de ejemplo. */
    public byte[] plantilla() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet h = wb.createSheet("Repuestos");
            CellStyle cab = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            cab.setFont(f);
            Row r0 = h.createRow(0);
            for (int c = 0; c < COLUMNAS.size(); c++) {
                Cell cell = r0.createCell(c);
                cell.setCellValue(COLUMNAS.get(c));
                cell.setCellStyle(cab);
            }
            Object[][] ejemplos = {
                    {"FIL-ACE-001", "Filtro de aceite motor CAT C9", "FILTROS", "UNIDAD", 20, 5, "Compatible con excavadora 320D"},
                    {"ACE-HID-068", "Aceite hidráulico ISO 68", "LUBRICANTES", "LITRO", 200, 50, "Cilindro de 208 L"}};
            for (int i = 0; i < ejemplos.length; i++) {
                Row r = h.createRow(i + 1);
                for (int c = 0; c < ejemplos[i].length; c++) {
                    Object v = ejemplos[i][c];
                    if (v instanceof Integer n) r.createCell(c).setCellValue(n);
                    else r.createCell(c).setCellValue(String.valueOf(v));
                }
            }
            for (int c = 0; c < COLUMNAS.size(); c++) h.autoSizeColumn(c);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new NegocioException("No se pudo generar la plantilla");
        }
    }

    // ------------------------------------------------------------------ internos

    private boolean procesarFila(Row fila, Map<String, Integer> col, DataFormatter fmt, String codigo) {
        String nombre = texto(fila, col, "nombre", fmt);
        Integer minimo = entero(fila, col, "stock_minimo", fmt);
        Integer stock = entero(fila, col, "stock_actual", fmt);
        Optional<Repuesto> existente = repuestoRepository.findByCodigo(codigo);
        Repuesto r = existente.orElseGet(Repuesto::new);
        boolean nuevo = existente.isEmpty();
        if (nuevo && nombre.isEmpty()) {
            throw new NegocioException("el nombre es obligatorio para un repuesto nuevo");
        }
        if (codigo.length() > 30) throw new NegocioException("el código supera 30 caracteres");
        if (nombre.length() > 150) throw new NegocioException("el nombre supera 150 caracteres");
        r.setCodigo(codigo);
        if (!nombre.isEmpty()) r.setNombre(nombre);
        String cat = texto(fila, col, "categoria", fmt);
        if (!cat.isEmpty()) r.setCategoria(categoria(cat));
        String unidad = texto(fila, col, "unidad_medida", fmt);
        if (!unidad.isEmpty()) r.setUnidadMedida(unidad.toUpperCase());
        if (minimo != null) r.setStockMinimo(minimo);
        String desc = texto(fila, col, "descripcion", fmt);
        if (!desc.isEmpty()) r.setDescripcion(desc);
        if (nuevo) {
            r.setStockActual(0);
            r.setEstado(true);
        }
        r = repuestoRepository.save(r);
        if (nuevo && stock != null && stock > 0) {
            MovimientoRequest mr = new MovimientoRequest();
            mr.setRepuestoId(r.getId());
            mr.setTipoMovimiento("ENTRADA");
            mr.setCantidad(stock);
            mr.setMotivo("Stock inicial importado desde Excel");
            UsuarioActual.username().flatMap(usuarioRepository::findByUsername).ifPresent(u -> mr.setUsuarioId(u.getId()));
            movimientoService.registrar(mr);
        }
        return nuevo;
    }

    private CategoriaRepuesto categoria(String nombre) {
        String n = nombre.trim().toUpperCase();
        return categoriaRepository.findByNombre(n).orElseGet(() -> {
            CategoriaRepuesto c = new CategoriaRepuesto();
            c.setNombre(n);
            return categoriaRepository.save(c);
        });
    }

    private static Map<String, Integer> columnas(Row cabecera) {
        if (cabecera == null) throw new NegocioException("El archivo está vacío");
        Map<String, Integer> m = new HashMap<>();
        for (Cell c : cabecera) {
            String k = normalizar(c.toString());
            if (COLUMNAS.contains(k)) m.put(k, c.getColumnIndex());
        }
        if (!m.containsKey("codigo") || !m.containsKey("nombre")) {
            throw new NegocioException("La primera fila debe tener los encabezados de la plantilla (al menos codigo y nombre)");
        }
        return m;
    }

    static String normalizar(String s) {
        String t = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replace(' ', '_');
    }

    private static String texto(Row fila, Map<String, Integer> col, String nombre, DataFormatter fmt) {
        Integer i = col.get(nombre);
        if (fila == null || i == null) return "";
        return fmt.formatCellValue(fila.getCell(i)).trim();
    }

    private static Integer entero(Row fila, Map<String, Integer> col, String nombre, DataFormatter fmt) {
        String t = texto(fila, col, nombre, fmt);
        if (t.isEmpty()) return null;
        try {
            int v = (int) Double.parseDouble(t.replace(",", "."));
            if (v < 0) throw new NegocioException(nombre + " no puede ser negativo");
            return v;
        } catch (NumberFormatException e) {
            throw new NegocioException(nombre + " debe ser un número: '" + t + "'");
        }
    }
}
