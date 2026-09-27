package com.transmaqsur.sigem.config;

import com.transmaqsur.sigem.model.Alquiler;
import com.transmaqsur.sigem.model.Almacen;
import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Campana;
import com.transmaqsur.sigem.model.Cliente;
import com.transmaqsur.sigem.model.Compra;
import com.transmaqsur.sigem.model.Contacto;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.model.Cotizacion;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.Operacion;
import com.transmaqsur.sigem.model.Oportunidad;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.Proveedor;
import com.transmaqsur.sigem.model.Repuesto;
import com.transmaqsur.sigem.model.ServicioTransporte;
import com.transmaqsur.sigem.model.Solicitud;
import com.transmaqsur.sigem.model.UbicacionGps;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.Usuario;
import com.transmaqsur.sigem.model.Venta;
import com.transmaqsur.sigem.model.enums.AreaEmpresa;
import com.transmaqsur.sigem.model.enums.CanalMarketing;
import com.transmaqsur.sigem.model.enums.CargoEmpleado;
import com.transmaqsur.sigem.model.enums.EstadoCampana;
import com.transmaqsur.sigem.model.enums.EtapaOportunidad;
import com.transmaqsur.sigem.model.enums.FuenteGps;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoCliente;
import com.transmaqsur.sigem.model.enums.TipoComprobante;
import com.transmaqsur.sigem.model.enums.TipoDocumento;
import com.transmaqsur.sigem.model.enums.TipoMantenimiento;
import com.transmaqsur.sigem.model.enums.TipoServicio;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.NotificacionRepository;
import com.transmaqsur.sigem.repository.RolRepository;
import com.transmaqsur.sigem.repository.UbicacionGpsRepository;
import com.transmaqsur.sigem.repository.UnidadRepository;
import com.transmaqsur.sigem.service.AlquilerService;
import com.transmaqsur.sigem.service.AsignacionService;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.CompraService;
import com.transmaqsur.sigem.service.ContratoService;
import com.transmaqsur.sigem.service.CotizacionService;
import com.transmaqsur.sigem.service.EmpleadoService;
import com.transmaqsur.sigem.service.InventarioService;
import com.transmaqsur.sigem.service.MantenimientoService;
import com.transmaqsur.sigem.service.MarketingService;
import com.transmaqsur.sigem.service.OperacionService;
import com.transmaqsur.sigem.service.ServicioTransporteService;
import com.transmaqsur.sigem.service.SolicitudService;
import com.transmaqsur.sigem.service.UnidadService;
import com.transmaqsur.sigem.service.UsuarioService;
import com.transmaqsur.sigem.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Carga datos de demostración realistas usando los servicios del sistema
 * (respetan todas las reglas de negocio). Solo se ejecuta si
 * {@code sigem.datos-demo=true} y aún no existen unidades registradas.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DatosDemo implements ApplicationRunner {

    private static final String CLAVE_DEMO = "sigem123";

    @Value("${sigem.datos-demo:true}")
    private boolean habilitado;

    private final UnidadRepository unidadRepository;
    private final RolRepository rolRepository;
    private final UbicacionGpsRepository gpsRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioService usuarioService;
    private final EmpleadoService empleadoService;
    private final ClienteService clienteService;
    private final MarketingService marketingService;
    private final UnidadService unidadService;
    private final InventarioService inventarioService;
    private final CompraService compraService;
    private final SolicitudService solicitudService;
    private final CotizacionService cotizacionService;
    private final ContratoService contratoService;
    private final AlquilerService alquilerService;
    private final ServicioTransporteService servicioService;
    private final AsignacionService asignacionService;
    private final MantenimientoService mantenimientoService;
    private final VentaService ventaService;

    private final LocalDate hoy = LocalDate.now();

    @Override
    public void run(ApplicationArguments args) {
        if (!habilitado || unidadRepository.count() > 0) {
            return;
        }
        log.info("Cargando datos de demostración de TRANSMAQ SUR S.A.C. ...");

        // ---------------------------------------------------------------- Empleados
        Empleado juan = empleado("41235678", "Juan", "Quispe Mamani", CargoEmpleado.CONDUCTOR, AreaEmpresa.OPERACIONES, "959112233", "Q41235678", "A-IIIc", hoy.plusYears(2));
        Empleado pedro = empleado("42345671", "Pedro", "Condori Huamán", CargoEmpleado.CONDUCTOR, AreaEmpresa.OPERACIONES, "958223344", "Q42345671", "A-IIIc", hoy.plusYears(1));
        Empleado luis = empleado("43456782", "Luis", "Apaza Choque", CargoEmpleado.CONDUCTOR, AreaEmpresa.OPERACIONES, "957334455", "Q43456782", "A-IIIb", hoy.plusMonths(18));
        Empleado carlos = empleado("44567893", "Carlos", "Ccama Flores", CargoEmpleado.CONDUCTOR, AreaEmpresa.OPERACIONES, "956445566", "Q44567893", "A-IIIc", hoy.plusYears(3));
        Empleado miguel = empleado("45678904", "Miguel", "Huanca Torres", CargoEmpleado.OPERADOR, AreaEmpresa.OPERACIONES, "955556677", "Q45678904", "A-IIb", hoy.plusYears(2));
        Empleado jorge = empleado("46789015", "Jorge", "Mamani Ticona", CargoEmpleado.OPERADOR, AreaEmpresa.OPERACIONES, "954667788", "Q46789015", "A-IIb", hoy.plusYears(1));
        Empleado rosa = empleado("47890126", "Rosa", "Puma Cáceres", CargoEmpleado.OPERADOR, AreaEmpresa.OPERACIONES, "953778899", "Q47890126", "A-IIb", hoy.plusMonths(30));
        empleado("48901237", "Víctor", "Vilca Soncco", CargoEmpleado.OPERADOR, AreaEmpresa.OPERACIONES, "952889900", "Q48901237", "A-IIb", hoy.plusDays(20));
        Empleado raul = empleado("40123459", "Raúl", "Chávez Paredes", CargoEmpleado.TECNICO, AreaEmpresa.MANTENIMIENTO, "951990011", null, null, null);
        Empleado edwin = empleado("40234561", "Edwin", "Salas Núñez", CargoEmpleado.JEFE_MANTENIMIENTO, AreaEmpresa.MANTENIMIENTO, "950001122", null, null, null);
        Empleado ana = empleado("40345672", "Ana", "Torres Delgado", CargoEmpleado.JEFE_OPERACIONES, AreaEmpresa.OPERACIONES, "949112233", null, null, null);
        Empleado maria = empleado("40456783", "María", "Zúñiga Rivera", CargoEmpleado.ASISTENTE_ADMINISTRATIVO, AreaEmpresa.ADMINISTRACION_LOGISTICA, "948223344", null, null, null);
        Empleado carla = empleado("40567894", "Carla", "Medina Portugal", CargoEmpleado.COORDINADOR_COMERCIAL, AreaEmpresa.COMERCIAL, "947334455", null, null, null);
        Empleado sofia = empleado("40678905", "Sofía", "Linares Bustamante", CargoEmpleado.ANALISTA_RRHH, AreaEmpresa.RECURSOS_HUMANOS, "946445566", null, null, null);
        Empleado oscar = empleado("40789016", "Óscar", "Rodríguez Valdivia", CargoEmpleado.ALMACENERO, AreaEmpresa.ADMINISTRACION_LOGISTICA, "945556677", null, null, null);
        Empleado fernando = empleado("40890127", "Fernando", "Díaz Cornejo", CargoEmpleado.GERENTE, AreaEmpresa.GERENCIA, "944667788", null, null, null);

        // ---------------------------------------------------------------- Usuarios
        usuario("gerente", fernando, "GERENCIA");
        usuario("operaciones", ana, "JEFE_OPERACIONES");
        usuario("mantenimiento", edwin, "JEFE_MANTENIMIENTO");
        usuario("tecnico", raul, "TECNICO");
        usuario("administracion", maria, "ASISTENTE_ADMINISTRATIVO");
        usuario("comercial", carla, "COORDINADOR_COMERCIAL");
        usuario("rrhh", sofia, "RECURSOS_HUMANOS");
        usuario("conductor", juan, "CONDUCTOR_OPERADOR");
        usuario("operador", miguel, "CONDUCTOR_OPERADOR");

        // ---------------------------------------------------------------- Clientes
        Cliente misti = cliente(TipoDocumento.RUC, "20455678901", "Constructora Misti S.A.C.", TipoCliente.CONSTRUCTORA,
                "Av. Ejército 710, Yanahuara, Arequipa", "054-251100", "obras@constructoramisti.pe");
        contacto(misti, "Ing. Ricardo Salinas", "Residente de obra", "959700100", "rsalinas@constructoramisti.pe", true);
        contacto(misti, "Lucía Benavides", "Logística", "959700200", "lbenavides@constructoramisti.pe", false);
        Cliente minera = cliente(TipoDocumento.RUC, "20498765432", "Contratistas Mineros del Sur S.A.", TipoCliente.MINERA,
                "Carretera a Yura km 12, Arequipa", "054-448800", "operaciones@cmsur.pe");
        contacto(minera, "Ing. Patricia Gómez", "Superintendente de operaciones", "958800300", "pgomez@cmsur.pe", true);
        Cliente muni = cliente(TipoDocumento.RUC, "20154477001", "Municipalidad Distrital de Cerro Colorado", TipoCliente.GOBIERNO_LOCAL,
                "Plaza principal s/n, Cerro Colorado", "054-382590", "obras@municerrocolorado.gob.pe");
        contacto(muni, "Arq. Daniel Rivera", "Gerente de Obras Públicas", "957900400", "drivera@municerrocolorado.gob.pe", true);
        Cliente chachani = cliente(TipoDocumento.RUC, "20601234567", "Ingeniería y Obras Chachani E.I.R.L.", TipoCliente.CONSTRUCTORA,
                "Calle Mercaderes 320, Cercado", "054-203040", "contacto@chachani.pe");
        contacto(chachani, "Ing. Martín Ochoa", "Gerente general", "956100500", "mochoa@chachani.pe", true);
        Cliente majes = cliente(TipoDocumento.RUC, "20512349876", "Agroindustrias Majes S.A.C.", TipoCliente.EMPRESA_PRIVADA,
                "Pedregal, Majes, Caylloma", "054-586600", "logistica@agromajes.pe");
        Cliente roberto = cliente(TipoDocumento.DNI, "29654321", "Roberto Paredes Linares", TipoCliente.PERSONA_NATURAL,
                "Urb. La Libertad B-12, Cerro Colorado", "959123456", null);

        // ---------------------------------------------------------------- Marketing
        Campana municipal = campana("Temporada de obras municipales 2026", CanalMarketing.VISITA_COMERCIAL, hoy.minusDays(45), hoy.plusDays(60),
                "3500", EstadoCampana.ACTIVA, "Municipalidades de Arequipa");
        Campana feria = campana("Feria minera del sur", CanalMarketing.FERIA, hoy.minusDays(120), hoy.minusDays(117),
                "12000", EstadoCampana.FINALIZADA, "Contratistas mineros");
        campana("Campaña digital de alquiler de maquinaria", CanalMarketing.REDES_SOCIALES, hoy.plusDays(10), hoy.plusDays(70),
                "2500", EstadoCampana.PLANIFICADA, "Pequeñas constructoras y agroindustria");
        Usuario comercial = usuarioService.listar().stream().filter(u -> u.getUsername().equals("comercial")).findFirst().orElseThrow();
        oportunidad("Pavimentación de vías en Cerro Colorado", muni, municipal, TipoServicio.ALQUILER, EtapaOportunidad.NEGOCIACION, "85000", comercial);
        oportunidad("Transporte de mineral a planta", minera, feria, TipoServicio.TRANSPORTE, EtapaOportunidad.GANADA, "120000", comercial);
        oportunidad("Movimiento de tierras en Majes", majes, null, TipoServicio.ALQUILER, EtapaOportunidad.CALIFICADA, "45000", comercial);
        oportunidad("Habilitación urbana Chachani II", chachani, municipal, TipoServicio.ALQUILER, EtapaOportunidad.PROSPECTO, "60000", comercial);
        oportunidad("Traslado de agregados para obra", misti, null, TipoServicio.TRANSPORTE, EtapaOportunidad.PERDIDA, "18000", comercial);

        // ---------------------------------------------------------------- Flota
        Unidad cam1 = unidad("CAM-001", TipoUnidad.CAMION_CARGA, "Volvo", "FH 460", 2020, "V1A-845", "30 t", "184200", "10000", "180000", "180", "1400", "6.50", hoy.plusMonths(7), hoy.plusMonths(4));
        Unidad cam2 = unidad("CAM-002", TipoUnidad.CAMION_CARGA, "Scania", "R450", 2019, "V2B-112", "30 t", "241800", "10000", "234500", "170", "1350", "6.20", hoy.plusDays(18), hoy.plusMonths(5));
        Unidad vol1 = unidad("VOL-001", TipoUnidad.VOLQUETE, "Volvo", "FMX 440 8x4", 2021, "V3C-339", "20 m³", "96500", "8000", "92000", "160", "1300", "7.00", hoy.plusMonths(9), hoy.plusMonths(6));
        Unidad vol2 = unidad("VOL-002", TipoUnidad.VOLQUETE, "Mercedes-Benz", "Actros 4144K", 2018, "V4D-771", "17 m³", "311000", "8000", "304500", "150", "1250", "6.80", hoy.plusMonths(3), hoy.plusMonths(2));
        Unidad cis1 = unidad("CIS-001", TipoUnidad.CISTERNA, "International", "7600 SBA", 2019, "V5E-208", "9 000 gal", "149200", "10000", "145000", "150", "1200", "6.00", hoy.plusMonths(6), hoy.plusMonths(8));
        Unidad exc1 = unidad("EXC-001", TipoUnidad.EXCAVADORA, "Caterpillar", "320 GC", 2021, null, "1.2 m³", "5150", "250", "5050", "280", "2100", null, null, null);
        Unidad exc2 = unidad("EXC-002", TipoUnidad.EXCAVADORA, "Komatsu", "PC200-8", 2017, null, "1.0 m³", "12480", "250", "12250", "250", "1900", null, null, null);
        Unidad cfr1 = unidad("CFR-001", TipoUnidad.CARGADOR_FRONTAL, "Caterpillar", "950 GC", 2020, null, "3.1 m³", "7280", "250", "7100", "240", "1800", null, null, null);
        Unidad ret1 = unidad("RET-001", TipoUnidad.RETROEXCAVADORA, "JCB", "3CX", 2022, null, "1.0 m³", "2080", "250", "2000", "180", "1350", null, null, null);
        Unidad ret2 = unidad("RET-002", TipoUnidad.RETROEXCAVADORA, "Caterpillar", "420F2", 2019, null, "1.0 m³", "6390", "250", "6300", "175", "1300", null, null, null);
        Unidad com1 = unidad("COM-001", TipoUnidad.COMPACTADORA, "Dynapac", "CA250D", 2020, null, "10 t", "3890", "250", "3800", "160", "1200", null, null, null);
        Unidad com2 = unidad("COM-002", TipoUnidad.COMPACTADORA, "Bomag", "BW 211 D-40", 2016, null, "11 t", "9800", "250", "9700", "150", "1100", null, null, null);
        unidadService.cambiarFueraDeServicio(com2.getId());

        // ---------------------------------------------------------------- Almacenes, repuestos y compras
        Almacen central = almacen("Almacén Central", "Parque Industrial de Arequipa, Calle 3 Mz. F", oscar);
        Almacen obra = almacen("Almacén de campamento Yura", "Campamento km 12 carretera a Yura", null);
        Repuesto filAce = repuesto("FIL-ACE-01", "Filtro de aceite de motor", "Filtros", "UND", "85", "10");
        Repuesto filAir = repuesto("FIL-AIR-01", "Filtro de aire primario", "Filtros", "UND", "160", "6");
        Repuesto filCom = repuesto("FIL-COM-01", "Filtro de combustible", "Filtros", "UND", "70", "10");
        Repuesto filHid = repuesto("FIL-HID-01", "Filtro hidráulico", "Filtros", "UND", "210", "4");
        Repuesto aceite = repuesto("ACE-15W40", "Aceite de motor 15W-40", "Lubricantes", "GL", "95", "20");
        Repuesto hidra = repuesto("ACE-HID68", "Aceite hidráulico ISO 68", "Lubricantes", "GL", "88", "15");
        Repuesto grasa = repuesto("GRA-EP2", "Grasa EP2 multiuso", "Lubricantes", "KG", "28", "20");
        Repuesto pastillas = repuesto("PAS-FRE-01", "Pastillas de freno para camión", "Frenos", "JGO", "380", "4");
        Repuesto neumatico = repuesto("NEU-1200", "Neumático 12.00R24", "Neumáticos", "UND", "2150", "4");
        Repuesto zapata = repuesto("ZAP-ORU-01", "Zapata de oruga de excavadora", "Tren de rodaje", "UND", "420", "8");
        Repuesto bateria = repuesto("BAT-12V", "Batería 12V 150Ah", "Eléctrico", "UND", "690", "3");
        Repuesto diente = repuesto("DIE-CUC-01", "Diente de cucharón", "Herramientas de corte", "UND", "145", "12");

        Proveedor prov1 = proveedor("20456789012", "Repuestos Industriales del Sur S.A.C.", "Repuestos de maquinaria", "Ing. Hugo Llerena", "054-431020");
        Proveedor prov2 = proveedor("20567890123", "Lubricantes Arequipa E.I.R.L.", "Lubricantes y filtros", "Sra. Elena Cuadros", "054-265544");
        Proveedor prov3 = proveedor("20678901234", "Neumáticos y Servicios Andinos S.A.", "Neumáticos y baterías", "Sr. Julio Pinto", "054-380011");

        Compra oc1 = compra(prov1, central, hoy.minusDays(60));
        compraService.agregarDetalle(oc1.getId(), filAce.getId(), bd("40"), bd("78"));
        compraService.agregarDetalle(oc1.getId(), filAir.getId(), bd("15"), bd("150"));
        compraService.agregarDetalle(oc1.getId(), filCom.getId(), bd("30"), bd("65"));
        compraService.agregarDetalle(oc1.getId(), filHid.getId(), bd("12"), bd("198"));
        compraService.agregarDetalle(oc1.getId(), zapata.getId(), bd("16"), bd("405"));
        compraService.agregarDetalle(oc1.getId(), diente.getId(), bd("30"), bd("140"));
        compraService.aprobar(oc1.getId());
        compraService.recibir(oc1.getId(), "F001-004512");
        Compra oc2 = compra(prov2, central, hoy.minusDays(40));
        compraService.agregarDetalle(oc2.getId(), aceite.getId(), bd("60"), bd("92"));
        compraService.agregarDetalle(oc2.getId(), hidra.getId(), bd("40"), bd("85"));
        compraService.agregarDetalle(oc2.getId(), grasa.getId(), bd("50"), bd("27"));
        compraService.aprobar(oc2.getId());
        compraService.recibir(oc2.getId(), "F002-000981");
        Compra oc3 = compra(prov3, central, hoy.minusDays(30));
        compraService.agregarDetalle(oc3.getId(), neumatico.getId(), bd("6"), bd("2100"));
        compraService.agregarDetalle(oc3.getId(), bateria.getId(), bd("3"), bd("670"));
        compraService.agregarDetalle(oc3.getId(), pastillas.getId(), bd("8"), bd("370"));
        compraService.aprobar(oc3.getId());
        compraService.recibir(oc3.getId(), "F001-020077");
        inventarioService.transferir(central.getId(), obra.getId(), filAce.getId(), bd("8"));
        inventarioService.transferir(central.getId(), obra.getId(), grasa.getId(), bd("10"));
        Compra oc4 = compra(prov3, central, hoy.minusDays(1));
        compraService.agregarDetalle(oc4.getId(), neumatico.getId(), bd("4"), bd("2120"));
        compraService.agregarDetalle(oc4.getId(), bateria.getId(), bd("4"), bd("665"));

        // ---------------------------------------------------------------- Solicitudes, cotizaciones y contratos
        Solicitud sol1 = solicitud(misti, TipoServicio.ALQUILER, TipoUnidad.EXCAVADORA, 1, hoy.minusDays(70), hoy.plusDays(45),
                "Obra: Condominio Los Pinos, Yanahuara", null, "Excavación de sótanos y movimiento de tierras");
        Cotizacion cot1 = cotizacion(sol1, "Tarifas no incluyen IGV. Combustible por cuenta de TRANSMAQ SUR. Operador incluido.");
        cotizacionService.agregarDetalle(cot1.getId(), "Servicio de transporte de material excedente (volquete)", TipoUnidad.VOLQUETE,
                Modalidad.POR_VIAJE, bd("40"), bd("450"));
        cotizacionService.enviar(cot1.getId());
        cotizacionService.aceptar(cot1.getId());
        Contrato ctrMisti = contratoService.generarDesdeCotizacion(cot1.getId());

        Contrato ctrMinera = contrato(minera, "Transporte de mineral y agregados", hoy.minusDays(100), hoy.plusDays(20), "180000");
        Contrato ctrMuni = contrato(muni, "Alquiler de maquinaria para pavimentación de vías", hoy.minusDays(40), hoy.plusDays(120), "95000");

        Solicitud sol2 = solicitud(majes, TipoServicio.ALQUILER, TipoUnidad.RETROEXCAVADORA, 1, hoy.plusDays(12), hoy.plusDays(26),
                "Fundo Pedregal, Majes", null, "Apertura de zanjas para riego tecnificado");
        Cotizacion cot2 = cotizacion(sol2, "Validez 15 días. Traslado de la unidad incluido.");
        cotizacionService.enviar(cot2.getId());
        solicitud(chachani, TipoServicio.TRANSPORTE, TipoUnidad.VOLQUETE, 2, hoy.plusDays(5), hoy.plusDays(35),
                "Cantera La Poderosa, Cerro Colorado", "Obra Chachani II, Cayma", "Transporte de agregados (arena gruesa y piedra chancada)");

        // ---------------------------------------------------------------- Operaciones históricas (facturadas)
        LocalDate ini = hoy.withDayOfMonth(1);
        operacionPasada(servicio(minera, null, cam1, juan, ini.minusMonths(5).plusDays(4), 2, "Planta Yura", "Puerto de Matarani", "Concentrado de mineral", Modalidad.POR_KM), "420", true);
        operacionPasada(alquiler(misti, null, exc1, miguel, ini.minusMonths(5).plusDays(10), 6, Modalidad.POR_DIA, "Obra Umacollo"), "48", true);
        operacionPasada(servicio(minera, null, vol1, pedro, ini.minusMonths(4).plusDays(3), 1, "Cantera Yura", "Chancadora km 20", "Agregados", Modalidad.POR_VIAJE), "85", true);
        operacionPasada(alquiler(muni, null, cfr1, jorge, ini.minusMonths(4).plusDays(15), 5, Modalidad.POR_HORA, "Limpieza de torrenteras"), "38", true);
        operacionPasada(servicio(majes, null, cis1, luis, ini.minusMonths(3).plusDays(8), 3, "Arequipa", "Majes", "Agua para riego", Modalidad.POR_KM), "380", true);
        operacionPasada(alquiler(chachani, null, ret2, rosa, ini.minusMonths(3).plusDays(20), 4, Modalidad.POR_DIA, "Habilitación Chachani I"), "30", true);
        operacionPasada(servicio(minera, ctrMinera, cam2, carlos, ini.minusMonths(2).plusDays(5), 2, "Planta Yura", "Matarani", "Concentrado de mineral", Modalidad.POR_KM), "410", true);
        operacionPasada(alquiler(muni, ctrMuni, com1, rosa, hoy.minusDays(35), 6, Modalidad.POR_HORA, "Av. Aviación, Cerro Colorado"), "46", true);
        operacionPasada(servicio(minera, ctrMinera, vol2, pedro, hoy.minusDays(25), 1, "Cantera Yura", "Planta de concreto Uchumayo", "Piedra chancada", Modalidad.POR_KM), "96", true);
        operacionPasada(alquiler(misti, ctrMisti, exc1, miguel, hoy.minusDays(20), 7, Modalidad.POR_HORA, "Condominio Los Pinos"), "62", true);
        // Finalizada y pendiente de facturar
        operacionPasada(alquiler(muni, ctrMuni, ret1, jorge, hoy.minusDays(8), 5, Modalidad.POR_DIA, "Calle Los Arces, Cerro Colorado"), "42", false);

        // ---------------------------------------------------------------- Operaciones actuales
        Alquiler enCurso = alquiler(misti, ctrMisti, exc1, miguel, hoy.minusDays(5), 12, Modalidad.POR_HORA, "Condominio Los Pinos, Yanahuara");
        alquilerService.iniciar(enCurso.getId(), null, hoy.minusDays(5).atTime(7, 30));
        ServicioTransporte srvCurso = servicio(minera, ctrMinera, vol1, pedro, hoy, 1, "Cantera Yura", "Planta Cerro Verde - Uchumayo", "Agregados para concreto", Modalidad.POR_VIAJE);
        servicioService.iniciar(srvCurso.getId(), null, hoy.atTime(6, 0));
        alquiler(muni, ctrMuni, cfr1, jorge, hoy.plusDays(6), 5, Modalidad.POR_DIA, "Parque industrial de Cerro Colorado");
        servicio(misti, ctrMisti, cam1, juan, hoy.plusDays(1), 1, "Almacén Constructora Misti", "Condominio Los Pinos", "Fierro de construcción", Modalidad.POR_KM);

        Asignacion interna = new Asignacion();
        interna.setUnidad(ret2);
        interna.setEmpleado(rosa);
        interna.setFechaInicio(hoy.plusDays(10).atTime(8, 0));
        interna.setFechaFin(hoy.plusDays(11).atTime(17, 0));
        interna.setDescripcion("Nivelación de patio de maniobras de la base");
        asignacionService.crearInterna(interna);

        // ---------------------------------------------------------------- Mantenimiento
        OrdenMantenimiento otCis = orden(cis1, TipoMantenimiento.PREVENTIVO, Prioridad.MEDIA, hoy.minusDays(20), raul,
                "Preventivo de 150 000 km: cambio de aceite, filtros y revisión de frenos");
        mantenimientoService.iniciar(otCis.getId());
        mantenimientoService.agregarRepuesto(otCis.getId(), filAce.getId(), central.getId(), bd("2"));
        mantenimientoService.agregarRepuesto(otCis.getId(), filCom.getId(), central.getId(), bd("2"));
        mantenimientoService.agregarRepuesto(otCis.getId(), aceite.getId(), central.getId(), bd("9"));
        mantenimientoService.completar(otCis.getId(), "Se cambió aceite 15W-40 (9 gal), filtros de aceite y combustible. Frenos en buen estado.",
                bd("350"), BigDecimal.ZERO, null);
        OrdenMantenimiento otExc = orden(exc2, TipoMantenimiento.CORRECTIVO, Prioridad.ALTA, hoy, raul,
                "Fuga de aceite en cilindro hidráulico del brazo y desgaste de zapatas de oruga");
        mantenimientoService.iniciar(otExc.getId());
        mantenimientoService.agregarRepuesto(otExc.getId(), hidra.getId(), central.getId(), bd("12"));
        mantenimientoService.agregarRepuesto(otExc.getId(), filHid.getId(), central.getId(), bd("1"));
        mantenimientoService.agregarRepuesto(otExc.getId(), zapata.getId(), central.getId(), bd("4"));
        orden(vol2, TipoMantenimiento.PREVENTIVO, Prioridad.MEDIA, hoy.plusDays(3), raul,
                "Preventivo 312 000 km: cambio de aceite y filtros, engrase general, revisión de suspensión");

        // Venta de repuestos a persona natural (boleta)
        Venta boleta = new Venta();
        boleta.setTipoComprobante(TipoComprobante.BOLETA);
        boleta.setCliente(roberto);
        boleta.setFechaEmision(hoy.minusDays(3));
        boleta = ventaService.guardar(boleta);
        ventaService.agregarRepuesto(boleta.getId(), filAce.getId(), central.getId(), bd("2"), bd("110"));
        ventaService.agregarRepuesto(boleta.getId(), bateria.getId(), central.getId(), bd("1"), bd("850"));
        ventaService.emitir(boleta.getId());
        ventaService.registrarPago(boleta.getId(), "Efectivo", hoy.minusDays(3));

        // ---------------------------------------------------------------- GPS
        gpsRuta(vol1, hoy.atTime(6, 5), new double[][]{{-16.418, -71.520}, {-16.395, -71.545}, {-16.360, -71.590}, {-16.330, -71.640},
                {-16.300, -71.670}, {-16.285, -71.690}});
        gpsRuta(exc1, hoy.atTime(7, 40), new double[][]{{-16.3905, -71.5480}, {-16.3907, -71.5483}});
        gpsRuta(cam1, hoy.atTime(8, 0), new double[][]{{-16.4180, -71.5200}});
        gpsRuta(cfr1, hoy.atTime(8, 0), new double[][]{{-16.4176, -71.5205}});
        gpsRuta(exc2, hoy.atTime(8, 0), new double[][]{{-16.4182, -71.5196}});
        gpsRuta(ret1, hoy.minusDays(3).atTime(17, 0), new double[][]{{-16.3700, -71.5600}});

        // Las notificaciones de la carga histórica no aportan; las alertas vigentes se generan al terminar el arranque
        notificacionRepository.deleteAll();

        log.info("Datos de demostración cargados. Usuarios: admin/admin123 y gerente, operaciones, mantenimiento, tecnico, "
                + "administracion, comercial, rrhh, conductor, operador con clave {}", CLAVE_DEMO);
    }

    // ==================================================================== helpers

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    private Empleado empleado(String dni, String nombres, String apellidos, CargoEmpleado cargo, AreaEmpresa area, String tel,
                              String licencia, String categoria, LocalDate vencimiento) {
        Empleado e = new Empleado();
        e.setDni(dni);
        e.setNombres(nombres);
        e.setApellidos(apellidos);
        e.setCargo(cargo);
        e.setArea(area);
        e.setTelefono(tel);
        e.setEmail(nombres.toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o")
                .replace("ú", "u") + "." + apellidos.split(" ")[0].toLowerCase().replace("á", "a").replace("ú", "u")
                .replace("í", "i").replace("é", "e").replace("ó", "o") + "@transmaqsur.pe");
        e.setFechaIngreso(hoy.minusYears(2).minusDays(dni.hashCode() % 300 + 300));
        e.setLicenciaNumero(licencia);
        e.setLicenciaCategoria(categoria);
        e.setLicenciaVencimiento(vencimiento);
        e.setDireccion("Arequipa");
        return empleadoService.guardar(e);
    }

    private void usuario(String username, Empleado empleado, String rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto(empleado.getNombreCompleto());
        u.setEmail(empleado.getEmail());
        u.setEmpleado(empleado);
        u.setRol(rolRepository.findByNombre(rol).orElseThrow());
        usuarioService.guardar(u, CLAVE_DEMO);
    }

    private Cliente cliente(TipoDocumento td, String doc, String razon, TipoCliente tipo, String dir, String tel, String email) {
        Cliente c = new Cliente();
        c.setTipoDocumento(td);
        c.setNumeroDocumento(doc);
        c.setRazonSocial(razon);
        c.setTipoCliente(tipo);
        c.setDireccion(dir);
        c.setTelefono(tel);
        c.setEmail(email);
        return clienteService.guardar(c);
    }

    private void contacto(Cliente cliente, String nombre, String cargo, String tel, String email, boolean principal) {
        Contacto c = new Contacto();
        c.setNombres(nombre);
        c.setCargo(cargo);
        c.setTelefono(tel);
        c.setEmail(email);
        c.setPrincipal(principal);
        clienteService.agregarContacto(cliente.getId(), c);
    }

    private Campana campana(String nombre, CanalMarketing canal, LocalDate ini, LocalDate fin, String presupuesto,
                            EstadoCampana estado, String publico) {
        Campana c = new Campana();
        c.setNombre(nombre);
        c.setCanal(canal);
        c.setFechaInicio(ini);
        c.setFechaFin(fin);
        c.setPresupuesto(bd(presupuesto));
        c.setEstado(estado);
        c.setPublicoObjetivo(publico);
        c.setDescripcion("Campaña orientada a " + publico.toLowerCase() + ".");
        return marketingService.guardarCampana(c);
    }

    private void oportunidad(String titulo, Cliente cliente, Campana campana, TipoServicio tipo, EtapaOportunidad etapa,
                             String monto, Usuario responsable) {
        Oportunidad o = new Oportunidad();
        o.setTitulo(titulo);
        o.setCliente(cliente);
        o.setCampana(campana);
        o.setTipoServicio(tipo);
        o.setEtapa(EtapaOportunidad.PROSPECTO);
        o.setMontoEstimado(bd(monto));
        o.setResponsable(responsable);
        o.setFechaCierreEstimada(hoy.plusDays(30));
        o = marketingService.guardarOportunidad(o);
        if (etapa != EtapaOportunidad.PROSPECTO) {
            marketingService.cambiarEtapa(o.getId(), etapa);
        }
    }

    private Unidad unidad(String codigo, TipoUnidad tipo, String marca, String modelo, int anio, String placa, String capacidad,
                          String lectura, String intervalo, String ultimo, String tHora, String tDia, String tKm,
                          LocalDate soat, LocalDate revision) {
        Unidad u = new Unidad();
        u.setCodigo(codigo);
        u.setTipo(tipo);
        u.setMarca(marca);
        u.setModelo(modelo);
        u.setAnio(anio);
        u.setPlaca(placa);
        u.setNumeroSerie(marca.substring(0, 3).toUpperCase() + "-" + anio + "-" + codigo.replace("-", ""));
        u.setCapacidad(capacidad);
        u.setLecturaActual(bd(lectura));
        u.setIntervaloMantenimiento(bd(intervalo));
        u.setLecturaUltimoMantenimiento(bd(ultimo));
        u.setTarifaHora(bd(tHora));
        u.setTarifaDia(bd(tDia));
        u.setTarifaKm(tKm != null ? bd(tKm) : null);
        u.setFechaAdquisicion(LocalDate.of(anio, 3, 15));
        u.setVencimientoSoat(soat);
        u.setVencimientoRevisionTecnica(revision);
        return unidadService.guardar(u);
    }

    private Almacen almacen(String nombre, String ubicacion, Empleado responsable) {
        Almacen a = new Almacen();
        a.setNombre(nombre);
        a.setUbicacion(ubicacion);
        a.setResponsable(responsable);
        return inventarioService.guardarAlmacen(a);
    }

    private Repuesto repuesto(String codigo, String nombre, String categoria, String um, String precio, String minimo) {
        Repuesto r = new Repuesto();
        r.setCodigo(codigo);
        r.setNombre(nombre);
        r.setCategoria(categoria);
        r.setUnidadMedida(um);
        r.setPrecioUnitario(bd(precio));
        r.setStockMinimo(bd(minimo));
        return inventarioService.guardarRepuesto(r);
    }

    private Proveedor proveedor(String ruc, String razon, String rubro, String contacto, String tel) {
        Proveedor p = new Proveedor();
        p.setRuc(ruc);
        p.setRazonSocial(razon);
        p.setRubro(rubro);
        p.setContacto(contacto);
        p.setTelefono(tel);
        p.setDireccion("Arequipa");
        return compraService.guardarProveedor(p);
    }

    private Compra compra(Proveedor proveedor, Almacen almacen, LocalDate fecha) {
        Compra c = new Compra();
        c.setProveedor(proveedor);
        c.setAlmacen(almacen);
        c.setFechaEmision(fecha);
        return compraService.guardar(c);
    }

    private Solicitud solicitud(Cliente cliente, TipoServicio tipo, TipoUnidad tipoUnidad, int cantidad, LocalDate ini, LocalDate fin,
                                String origen, String destino, String descripcion) {
        Solicitud s = new Solicitud();
        s.setCliente(cliente);
        s.setTipoServicio(tipo);
        s.setTipoUnidad(tipoUnidad);
        s.setCantidadUnidades(cantidad);
        s.setFechaSolicitud(ini.isBefore(hoy) ? ini.minusDays(10) : hoy.minusDays(2));
        s.setFechaInicio(ini);
        s.setFechaFin(fin);
        s.setOrigen(origen);
        s.setDestino(destino);
        s.setDescripcion(descripcion);
        return solicitudService.guardar(s);
    }

    private Cotizacion cotizacion(Solicitud s, String condiciones) {
        Cotizacion c = new Cotizacion();
        c.setSolicitud(s);
        c.setCliente(s.getCliente());
        c.setFechaEmision(s.getFechaSolicitud().plusDays(1));
        c.setDiasValidez(s.getFechaSolicitud().isBefore(hoy.minusDays(20)) ? 90 : 15);
        c.setCondiciones(condiciones);
        return cotizacionService.guardar(c);
    }

    private Contrato contrato(Cliente cliente, String objeto, LocalDate ini, LocalDate fin, String monto) {
        Contrato c = new Contrato();
        c.setCliente(cliente);
        c.setObjeto(objeto);
        c.setFechaInicio(ini);
        c.setFechaFin(fin);
        c.setMontoTotal(bd(monto));
        c.setCondiciones("Valorizaciones quincenales. Pago a 30 días de emitida la factura. Penalidad por demora según anexo.");
        return contratoService.guardar(c);
    }

    private Alquiler alquiler(Cliente cliente, Contrato contrato, Unidad unidad, Empleado operador, LocalDate ini, int dias,
                              Modalidad modalidad, String obra) {
        Alquiler a = new Alquiler();
        a.setCliente(cliente);
        a.setContrato(contrato);
        a.setModalidad(modalidad);
        a.setUbicacionObra(obra);
        a.getAsignacion().setUnidad(unidad);
        a.getAsignacion().setEmpleado(operador);
        a.getAsignacion().setFechaInicio(ini.atTime(7, 0));
        a.getAsignacion().setFechaFin(ini.plusDays(dias - 1L).atTime(18, 0));
        return alquilerService.guardar(a);
    }

    private ServicioTransporte servicio(Cliente cliente, Contrato contrato, Unidad unidad, Empleado conductor, LocalDate ini, int dias,
                                        String origen, String destino, String carga, Modalidad modalidad) {
        ServicioTransporte s = new ServicioTransporte();
        s.setCliente(cliente);
        s.setContrato(contrato);
        s.setModalidad(modalidad);
        s.setOrigen(origen);
        s.setDestino(destino);
        s.setTipoCarga(carga);
        s.setPesoToneladas(bd("28"));
        if (modalidad == Modalidad.POR_VIAJE) {
            s.setTarifa(bd("950"));
        }
        s.getAsignacion().setUnidad(unidad);
        s.getAsignacion().setEmpleado(conductor);
        s.getAsignacion().setFechaInicio(ini.atTime(6, 0));
        s.getAsignacion().setFechaFin(ini.plusDays(dias - 1L).atTime(20, 0));
        return servicioService.guardar(s);
    }

    /** Ejecuta y cierra una operación en las fechas planificadas y, si corresponde, la factura. */
    private void operacionPasada(Operacion op, String uso, boolean facturar) {
        OperacionService<?> svc = op instanceof Alquiler ? alquilerService : servicioService;
        LocalDateTime inicio = op.getAsignacion().getFechaInicio();
        LocalDateTime fin = op.getAsignacion().getFechaFin().minusHours(1);
        svc.iniciar(op.getId(), null, inicio);
        BigDecimal lectura = unidadService.obtener(op.getUnidad().getId()).getLecturaActual().add(bd(uso));
        svc.finalizar(op.getId(), lectura, fin);
        if (!facturar) {
            return;
        }
        String tipo = op instanceof Alquiler ? "A" : "S";
        Venta v = new Venta();
        v.setCliente(op.getCliente());
        v.setTipoComprobante(TipoComprobante.FACTURA);
        v.setFechaEmision(fin.toLocalDate().plusDays(1).isAfter(hoy) ? hoy : fin.toLocalDate().plusDays(1));
        v = ventaService.guardar(v);
        ventaService.agregarOperacion(v.getId(), tipo + "-" + op.getId());
        ventaService.emitir(v.getId());
        if (v.getFechaEmision().isBefore(hoy.minusDays(30))) {
            ventaService.registrarPago(v.getId(), "Transferencia bancaria", v.getFechaEmision().plusDays(25));
        }
    }

    private OrdenMantenimiento orden(Unidad unidad, TipoMantenimiento tipo, Prioridad prioridad, LocalDate fecha, Empleado tecnico,
                                     String descripcion) {
        OrdenMantenimiento o = new OrdenMantenimiento();
        o.setUnidad(unidad);
        o.setTipo(tipo);
        o.setPrioridad(prioridad);
        o.setFechaProgramada(fecha);
        o.setTecnico(tecnico);
        o.setDescripcion(descripcion);
        return mantenimientoService.guardar(o);
    }

    private void gpsRuta(Unidad unidad, LocalDateTime desde, double[][] puntos) {
        for (int i = 0; i < puntos.length; i++) {
            UbicacionGps g = new UbicacionGps();
            g.setUnidad(unidad);
            g.setLatitud(puntos[i][0]);
            g.setLongitud(puntos[i][1]);
            g.setVelocidad(puntos.length > 2 ? 45.0 + i * 3 : 0.0);
            g.setFechaHora(desde.plusMinutes(i * 20L));
            g.setFuente(FuenteGps.SIMULADOR);
            g.setReferencia(i == 0 ? "Salida" : "En ruta");
            gpsRepository.save(g);
        }
    }

}
