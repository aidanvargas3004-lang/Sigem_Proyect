package com.transmaqsur.sigem;

import com.transmaqsur.sigem.model.Alquiler;
import com.transmaqsur.sigem.model.Cliente;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.AreaEmpresa;
import com.transmaqsur.sigem.model.enums.CargoEmpleado;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.TipoCliente;
import com.transmaqsur.sigem.model.enums.TipoDocumento;
import com.transmaqsur.sigem.model.enums.TipoUnidad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/** Datos de prueba reutilizables. */
public final class Fixtures {

    private static final AtomicInteger SEQ = new AtomicInteger(100);

    private Fixtures() {
    }

    public static Cliente cliente() {
        Cliente c = new Cliente();
        c.setTipoDocumento(TipoDocumento.RUC);
        c.setNumeroDocumento("20" + String.format("%09d", SEQ.incrementAndGet()));
        c.setRazonSocial("Constructora Prueba " + SEQ.get() + " S.A.C.");
        c.setTipoCliente(TipoCliente.CONSTRUCTORA);
        return c;
    }

    public static Unidad excavadora(String lectura) {
        Unidad u = new Unidad();
        u.setCodigo("EXC-T" + SEQ.incrementAndGet());
        u.setTipo(TipoUnidad.EXCAVADORA);
        u.setMarca("Caterpillar");
        u.setModelo("320");
        u.setLecturaActual(new BigDecimal(lectura));
        u.setLecturaUltimoMantenimiento(new BigDecimal(lectura));
        u.setIntervaloMantenimiento(new BigDecimal("250"));
        u.setTarifaHora(new BigDecimal("280"));
        u.setTarifaDia(new BigDecimal("2100"));
        return u;
    }

    public static Unidad camion(String km) {
        Unidad u = new Unidad();
        u.setCodigo("CAM-T" + SEQ.incrementAndGet());
        u.setTipo(TipoUnidad.CAMION_CARGA);
        u.setMarca("Volvo");
        u.setModelo("FH");
        u.setPlaca("T" + SEQ.get() + "-XY");
        u.setLecturaActual(new BigDecimal(km));
        u.setLecturaUltimoMantenimiento(new BigDecimal(km));
        u.setIntervaloMantenimiento(new BigDecimal("10000"));
        u.setTarifaKm(new BigDecimal("6.50"));
        return u;
    }

    public static Empleado operador() {
        Empleado e = new Empleado();
        e.setDni(String.format("%08d", 40000000 + SEQ.incrementAndGet()));
        e.setNombres("Operador");
        e.setApellidos("Prueba " + SEQ.get());
        e.setCargo(CargoEmpleado.OPERADOR);
        e.setArea(AreaEmpresa.OPERACIONES);
        e.setLicenciaNumero("Q" + SEQ.get());
        e.setLicenciaCategoria("A-IIb");
        e.setLicenciaVencimiento(LocalDate.now().plusYears(2));
        return e;
    }

    public static Alquiler alquiler(Cliente c, Unidad u, Empleado e, LocalDateTime inicio, LocalDateTime fin, Modalidad modalidad) {
        Alquiler a = new Alquiler();
        a.setCliente(c);
        a.setModalidad(modalidad);
        a.getAsignacion().setUnidad(u);
        a.getAsignacion().setEmpleado(e);
        a.getAsignacion().setFechaInicio(inicio);
        a.getAsignacion().setFechaFin(fin);
        return a;
    }
}
