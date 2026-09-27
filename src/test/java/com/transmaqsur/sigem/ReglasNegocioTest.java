package com.transmaqsur.sigem;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Alquiler;
import com.transmaqsur.sigem.model.Almacen;
import com.transmaqsur.sigem.model.Cliente;
import com.transmaqsur.sigem.model.Compra;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.model.Cotizacion;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.Proveedor;
import com.transmaqsur.sigem.model.Repuesto;
import com.transmaqsur.sigem.model.Solicitud;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.Venta;
import com.transmaqsur.sigem.model.enums.EstadoContrato;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.EstadoSolicitud;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.EstadoVenta;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoMantenimiento;
import com.transmaqsur.sigem.model.enums.TipoServicio;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.AuditoriaRepository;
import com.transmaqsur.sigem.service.AlquilerService;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.CompraService;
import com.transmaqsur.sigem.service.ContratoService;
import com.transmaqsur.sigem.service.CotizacionService;
import com.transmaqsur.sigem.service.EmpleadoService;
import com.transmaqsur.sigem.service.InventarioService;
import com.transmaqsur.sigem.service.MantenimientoService;
import com.transmaqsur.sigem.service.SolicitudService;
import com.transmaqsur.sigem.service.UnidadService;
import com.transmaqsur.sigem.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas de las reglas de negocio principales del SIGEM. */
@SpringBootTest
@ActiveProfiles("test")
class ReglasNegocioTest {

    @Autowired ClienteService clienteService;
    @Autowired UnidadService unidadService;
    @Autowired EmpleadoService empleadoService;
    @Autowired AlquilerService alquilerService;
    @Autowired MantenimientoService mantenimientoService;
    @Autowired InventarioService inventarioService;
    @Autowired CompraService compraService;
    @Autowired VentaService ventaService;
    @Autowired SolicitudService solicitudService;
    @Autowired CotizacionService cotizacionService;
    @Autowired ContratoService contratoService;
    @Autowired AuditoriaRepository auditoriaRepository;
    @Autowired TransactionTemplate transaccion;

    Cliente cliente;
    Unidad excavadora;
    Empleado operador;
    final LocalDateTime lunes = LocalDate.now().plusDays(10).atTime(7, 0);

    @BeforeEach
    void preparar() {
        cliente = clienteService.guardar(Fixtures.cliente());
        excavadora = unidadService.guardar(Fixtures.excavadora("1000"));
        operador = empleadoService.guardar(Fixtures.operador());
    }

    // ------------------------------------------------------------ Asignaciones

    @Test
    void noPermiteAsignarLaMismaUnidadEnHorariosCruzados() {
        alquilerService.guardar(Fixtures.alquiler(cliente, excavadora, operador, lunes, lunes.plusDays(3), Modalidad.POR_DIA));
        Empleado otro = empleadoService.guardar(Fixtures.operador());

        assertThatThrownBy(() -> alquilerService.guardar(
                Fixtures.alquiler(cliente, excavadora, otro, lunes.plusDays(2), lunes.plusDays(5), Modalidad.POR_DIA)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("ya está asignada");
        assertThat(unidadService.obtener(excavadora.getId()).getEstado()).isEqualTo(EstadoUnidad.ASIGNADA);
    }

    @Test
    void noPermiteAsignarAlMismoOperadorADosUnidadesALaVez() {
        alquilerService.guardar(Fixtures.alquiler(cliente, excavadora, operador, lunes, lunes.plusDays(3), Modalidad.POR_DIA));
        Unidad otra = unidadService.guardar(Fixtures.excavadora("500"));

        assertThatThrownBy(() -> alquilerService.guardar(
                Fixtures.alquiler(cliente, otra, operador, lunes.plusDays(1), lunes.plusDays(2), Modalidad.POR_DIA)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("ya está asignado");
    }

    @Test
    void permiteAsignacionesConsecutivasSinCruce() {
        alquilerService.guardar(Fixtures.alquiler(cliente, excavadora, operador, lunes, lunes.plusHours(10), Modalidad.POR_HORA));
        Alquiler segundo = alquilerService.guardar(
                Fixtures.alquiler(cliente, excavadora, operador, lunes.plusHours(10), lunes.plusHours(20), Modalidad.POR_HORA));
        assertThat(segundo.getCodigo()).startsWith("ALQ-");
    }

    // ------------------------------------------------------------ Operación y facturación

    @Test
    void cicloCompletoDeAlquilerCalculaHorasFacturablesYFactura() {
        Alquiler a = alquilerService.guardar(
                Fixtures.alquiler(cliente, excavadora, operador, lunes, lunes.plusDays(1), Modalidad.POR_HORA));
        assertThat(a.getTarifa()).isEqualByComparingTo("280"); // tomada de la unidad

        alquilerService.iniciar(a.getId(), null, lunes);
        assertThat(unidadService.obtener(excavadora.getId()).getEstado()).isEqualTo(EstadoUnidad.EN_SERVICIO);

        alquilerService.finalizar(a.getId(), new BigDecimal("1009.5"), lunes.plusHours(10));
        Alquiler fin = alquilerService.obtener(a.getId());
        assertThat(fin.getEstado()).isEqualTo(EstadoOperacion.FINALIZADO);
        assertThat(fin.getCantidadFacturable()).isEqualByComparingTo("9.5");
        assertThat(fin.getMontoTotal()).isEqualByComparingTo("2660.00");
        Unidad u = unidadService.obtener(excavadora.getId());
        assertThat(u.getEstado()).isEqualTo(EstadoUnidad.DISPONIBLE);
        assertThat(u.getLecturaActual()).isEqualByComparingTo("1009.5");

        Venta v = ventaService.facturarOperacion("A", a.getId());
        ventaService.emitir(v.getId());
        Venta emitida = ventaService.obtener(v.getId());
        assertThat(emitida.getEstado()).isEqualTo(EstadoVenta.EMITIDA);
        assertThat(emitida.getNumero()).isPositive();
        assertThat(emitida.getSubtotal()).isEqualByComparingTo("2660.00");
        assertThat(emitida.getIgv()).isEqualByComparingTo("478.80");
        assertThat(emitida.getTotal()).isEqualByComparingTo("3138.80");
        assertThat(alquilerService.obtener(a.getId()).isFacturado()).isTrue();

        // No se puede facturar dos veces la misma operación
        assertThatThrownBy(() -> ventaService.facturarOperacion("A", a.getId())).isInstanceOf(NegocioException.class);

        // Al anular el comprobante la operación vuelve a quedar pendiente de facturar
        ventaService.anular(v.getId());
        assertThat(alquilerService.obtener(a.getId()).isFacturable()).isTrue();
    }

    @Test
    void alquilerPorDiaCuentaDiasCalendario() {
        Alquiler a = alquilerService.guardar(
                Fixtures.alquiler(cliente, excavadora, null, lunes, lunes.plusDays(4), Modalidad.POR_DIA));
        alquilerService.iniciar(a.getId(), null, lunes);
        alquilerService.finalizar(a.getId(), new BigDecimal("1040"), lunes.plusDays(2).withHour(18));
        assertThat(alquilerService.obtener(a.getId()).getCantidadFacturable()).isEqualByComparingTo("3");
        assertThat(alquilerService.obtener(a.getId()).getMontoTotal()).isEqualByComparingTo("6300.00");
    }

    @Test
    void lecturaFinalNoPuedeSerMenorQueLaInicial() {
        Alquiler a = alquilerService.guardar(
                Fixtures.alquiler(cliente, excavadora, operador, lunes, lunes.plusDays(1), Modalidad.POR_HORA));
        alquilerService.iniciar(a.getId(), null, lunes);
        assertThatThrownBy(() -> alquilerService.finalizar(a.getId(), new BigDecimal("900"), lunes.plusHours(5)))
                .isInstanceOf(NegocioException.class);
    }

    // ------------------------------------------------------------ Mantenimiento e inventario

    @Test
    void mantenimientoBloqueaLaUnidadConsumeRepuestosYReiniciaElContador() {
        Almacen almacen = new Almacen();
        almacen.setNombre("Almacén prueba " + excavadora.getCodigo());
        almacen = inventarioService.guardarAlmacen(almacen);
        Repuesto filtro = new Repuesto();
        filtro.setCodigo("FIL-" + excavadora.getCodigo());
        filtro.setNombre("Filtro de prueba");
        filtro.setPrecioUnitario(new BigDecimal("80"));
        filtro.setStockMinimo(new BigDecimal("1"));
        filtro = inventarioService.guardarRepuesto(filtro);

        // Ingreso por compra
        Proveedor p = new Proveedor();
        p.setRuc("20" + String.format("%09d", excavadora.getId() + 500));
        p.setRazonSocial("Proveedor prueba");
        p = compraService.guardarProveedor(p);
        Compra oc = new Compra();
        oc.setProveedor(p);
        oc.setAlmacen(almacen);
        oc = compraService.guardar(oc);
        compraService.agregarDetalle(oc.getId(), filtro.getId(), new BigDecimal("5"), new BigDecimal("75"));
        compraService.aprobar(oc.getId());
        compraService.recibir(oc.getId(), "F001-1");
        assertThat(inventarioService.stock(almacen.getId(), filtro.getId())).isEqualByComparingTo("5");

        unidadService.registrarLectura(excavadora.getId(), new BigDecimal("1240")); // 96% del intervalo
        assertThat(unidadService.obtener(excavadora.getId()).isRequiereMantenimiento()).isTrue();

        OrdenMantenimiento ot = mantenimientoService.nuevaPreventiva(excavadora.getId());
        ot.setFechaProgramada(LocalDate.now());
        ot.setPrioridad(Prioridad.ALTA);
        ot = mantenimientoService.guardar(ot);
        mantenimientoService.iniciar(ot.getId());
        assertThat(unidadService.obtener(excavadora.getId()).getEstado()).isEqualTo(EstadoUnidad.EN_MANTENIMIENTO);

        // Mientras está en taller no puede asignarse a un alquiler que empiece hoy
        assertThatThrownBy(() -> alquilerService.guardar(Fixtures.alquiler(cliente, excavadora, operador,
                LocalDate.now().atTime(8, 0), LocalDate.now().atTime(18, 0), Modalidad.POR_HORA)))
                .isInstanceOf(NegocioException.class).hasMessageContaining("mantenimiento");

        mantenimientoService.agregarRepuesto(ot.getId(), filtro.getId(), almacen.getId(), new BigDecimal("2"));
        assertThat(inventarioService.stock(almacen.getId(), filtro.getId())).isEqualByComparingTo("3");
        Long otId = ot.getId();
        Long repId = filtro.getId();
        Long almId = almacen.getId();
        assertThatThrownBy(() -> mantenimientoService.agregarRepuesto(otId, repId, almId, new BigDecimal("10")))
                .isInstanceOf(NegocioException.class).hasMessageContaining("Stock insuficiente");

        mantenimientoService.completar(ot.getId(), "Cambio de filtros y aceite", new BigDecimal("300"), BigDecimal.ZERO, null);
        OrdenMantenimiento cerrada = mantenimientoService.obtener(ot.getId());
        assertThat(cerrada.getCostoRepuestos()).isEqualByComparingTo("150.00"); // 2 × último costo de compra (75)
        assertThat(cerrada.getCostoTotal()).isEqualByComparingTo("450.00");
        Unidad u = unidadService.obtener(excavadora.getId());
        assertThat(u.getEstado()).isEqualTo(EstadoUnidad.DISPONIBLE);
        assertThat(u.getLecturaUltimoMantenimiento()).isEqualByComparingTo("1240");
        assertThat(u.isRequiereMantenimiento()).isFalse();
    }

    // ------------------------------------------------------------ Comercial

    @Test
    void solicitudCotizacionYContrato() {
        Solicitud s = new Solicitud();
        s.setCliente(cliente);
        s.setTipoServicio(TipoServicio.ALQUILER);
        s.setTipoUnidad(TipoUnidad.EXCAVADORA);
        s.setFechaInicio(LocalDate.now().plusDays(5));
        s.setFechaFin(LocalDate.now().plusDays(9));
        s.setDescripcion("Excavación de zanjas");
        s = solicitudService.guardar(s);
        assertThat(s.getCodigo()).startsWith("SOL-");

        Cotizacion c = new Cotizacion();
        c.setSolicitud(s);
        c.setCliente(cliente);
        c = cotizacionService.guardar(c);
        Long idCot = c.getId();
        transaccion.executeWithoutResult(t -> {
            Cotizacion conLinea = cotizacionService.obtener(idCot);
            assertThat(conLinea.getDetalles()).hasSize(1); // línea sugerida: 5 días de excavadora
            assertThat(conLinea.getDetalles().get(0).getCantidad()).isEqualByComparingTo("5");
        });

        cotizacionService.enviar(c.getId());
        cotizacionService.aceptar(c.getId());
        assertThat(solicitudService.obtener(s.getId()).getEstado()).isEqualTo(EstadoSolicitud.APROBADA);

        Contrato ctr = contratoService.generarDesdeCotizacion(c.getId());
        assertThat(ctr.getEstado()).isEqualTo(EstadoContrato.VIGENTE);
        assertThat(ctr.getMontoTotal()).isEqualByComparingTo(cotizacionService.obtener(c.getId()).getTotal());
        assertThat(solicitudService.obtener(s.getId()).getEstado()).isEqualTo(EstadoSolicitud.ATENDIDA);
        Long cotId = c.getId();
        assertThatThrownBy(() -> contratoService.generarDesdeCotizacion(cotId)).isInstanceOf(NegocioException.class);

        Contrato renovado = contratoService.renovar(ctr.getId(), ctr.getFechaFin().plusMonths(2), null);
        assertThat(contratoService.obtener(ctr.getId()).getEstado()).isEqualTo(EstadoContrato.RENOVADO);
        assertThat(renovado.getContratoAnterior().getId()).isEqualTo(ctr.getId());
    }

    @Test
    void facturaRequiereClienteConRuc() {
        Cliente persona = Fixtures.cliente();
        persona.setTipoDocumento(com.transmaqsur.sigem.model.enums.TipoDocumento.DNI);
        persona.setNumeroDocumento(String.format("%08d", 29000000 + excavadora.getId()));
        persona.setTipoCliente(com.transmaqsur.sigem.model.enums.TipoCliente.PERSONA_NATURAL);
        Cliente guardado = clienteService.guardar(persona);
        Venta v = new Venta();
        v.setCliente(guardado);
        assertThatThrownBy(() -> ventaService.guardar(v)).isInstanceOf(NegocioException.class).hasMessageContaining("RUC");
    }

    // ------------------------------------------------------------ Auditoría

    @Test
    void registraAuditoriaDeCreacionYModificacion() {
        Cliente c = clienteService.guardar(Fixtures.cliente());
        c.setTelefono("054-111222");
        clienteService.guardar(c);
        var historial = auditoriaRepository.findByEntidadAndEntidadIdOrderByFechaDesc("Cliente", c.getId());
        assertThat(historial).extracting(a -> a.getAccion().name()).contains("CREAR", "ACTUALIZAR");
        assertThat(historial).anyMatch(a -> a.getDetalle().contains("telefono"));
    }
}
