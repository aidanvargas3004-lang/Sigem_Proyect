package com.transur.sigem.logistica;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.logistica.dto.MovimientoRequest;
import com.transur.sigem.logistica.dto.ResultadoImportacion;
import com.transur.sigem.logistica.entity.CategoriaRepuesto;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.CategoriaRepuestoRepository;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.logistica.service.MovimientoInventarioService;
import com.transur.sigem.logistica.service.RepuestoImportService;
import com.transur.sigem.security.repository.UsuarioRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Importación de ficheros: carga de repuestos desde Excel. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepuestoImportServiceTest {

    @Mock RepuestoRepository repuestoRepository;
    @Mock CategoriaRepuestoRepository categoriaRepository;
    @Mock MovimientoInventarioService movimientoService;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks RepuestoImportService service;

    private static byte[] excel(Object[]... filas) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet h = wb.createSheet();
            for (int i = 0; i < filas.length; i++) {
                Row r = h.createRow(i);
                for (int c = 0; c < filas[i].length; c++) {
                    Object v = filas[i][c];
                    if (v instanceof Number n) r.createCell(c).setCellValue(n.doubleValue());
                    else if (v != null) r.createCell(c).setCellValue(v.toString());
                }
            }
            wb.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void creaNuevosConEntradaDeStockYActualizaExistentes() throws Exception {
        Repuesto existente = new Repuesto();
        existente.setId(1L);
        existente.setCodigo("FIL-001");
        existente.setStockActual(7);
        when(repuestoRepository.findByCodigo("FIL-001")).thenReturn(Optional.of(existente));
        when(repuestoRepository.findByCodigo("ACE-068")).thenReturn(Optional.empty());
        when(categoriaRepository.findByNombre(any())).thenReturn(Optional.of(new CategoriaRepuesto()));
        when(repuestoRepository.save(any())).thenAnswer(inv -> {
            Repuesto r = inv.getArgument(0);
            if (r.getId() == null) r.setId(99L);
            return r;
        });
        byte[] xlsx = excel(
                new Object[]{"Código", "Nombre", "Categoría", "Unidad medida", "Stock actual", "Stock mínimo"},
                new Object[]{"fil-001", "Filtro aceite", "FILTROS", "UNIDAD", 50, 5},
                new Object[]{"ACE-068", "Aceite hidráulico", "LUBRICANTES", "LITRO", 200, 40},
                new Object[]{"BAD-1", "Malo", "X", "UNIDAD", "abc", 1});

        ResultadoImportacion r = service.importar("repuestos.xlsx", new ByteArrayInputStream(xlsx));

        assertThat(r.creados()).isEqualTo(1);
        assertThat(r.actualizados()).isEqualTo(1);
        assertThat(r.errores()).singleElement().asString().contains("Fila 4").contains("stock_actual");
        assertThat(existente.getStockActual()).isEqualTo(7); // el stock existente no se pisa
        ArgumentCaptor<MovimientoRequest> mov = ArgumentCaptor.forClass(MovimientoRequest.class);
        verify(movimientoService, times(1)).registrar(mov.capture());
        assertThat(mov.getValue().getTipoMovimiento()).isEqualTo("ENTRADA");
        assertThat(mov.getValue().getCantidad()).isEqualTo(200);
    }

    @Test
    void rechazaArchivosQueNoSonExcel() {
        assertThatThrownBy(() -> service.importar("datos.csv", new ByteArrayInputStream(new byte[0])))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void laPlantillaEsUnXlsxValido() {
        byte[] p = service.plantilla();
        assertThat(new String(p, 0, 2)).isEqualTo("PK");
    }
}
