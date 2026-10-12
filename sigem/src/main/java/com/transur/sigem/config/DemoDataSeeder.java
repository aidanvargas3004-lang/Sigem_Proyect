package com.transur.sigem.config;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.comercial.entity.*;
import com.transur.sigem.comercial.repository.*;
import com.transur.sigem.compras.entity.Proveedor;
import com.transur.sigem.compras.repository.ProveedorRepository;
import com.transur.sigem.logistica.entity.*;
import com.transur.sigem.logistica.repository.*;
import com.transur.sigem.maquinaria.entity.*;
import com.transur.sigem.maquinaria.repository.*;
import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import com.transur.sigem.mantenimiento.repository.MantenimientoRepository;
import com.transur.sigem.marketing.entity.*;
import com.transur.sigem.marketing.repository.*;
import com.transur.sigem.operaciones.entity.*;
import com.transur.sigem.operaciones.repository.*;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import com.transur.sigem.security.entity.Rol;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.RolRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Escenario demo completo para presentacion. Es idempotente: si ya existen
 * suficientes registros demo, no duplica la carga.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {
    private final ClienteRepository clienteRepo;
    private final UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final TipoMaquinariaRepository tipoMaquinariaRepo;
    private final MaquinariaRepository maquinariaRepo;
    private final TarifaRepository tarifaRepo;
    private final TipoServicioRepository tipoServicioRepo;
    private final EmpleadoRepository empleadoRepo;
    private final SolicitudRepository solicitudRepo;
    private final CotizacionRepository cotizacionRepo;
    private final ContratoRepository contratoRepo;
    private final ServicioRepository servicioRepo;
    private final AsignacionRepository asignacionRepo;
    private final IncidenciaRepository incidenciaRepo;
    private final CategoriaRepuestoRepository categoriaRepo;
    private final RepuestoRepository repuestoRepo;
    private final AlmacenRepository almacenRepo;
    private final UbicacionAlmacenRepository ubicacionRepo;
    private final InventarioMaquinariaRepository inventarioMaquinariaRepo;
    private final MovimientoInventarioRepository movimientoRepo;
    private final ProveedorRepository proveedorRepo;
    private final MantenimientoRepository mantenimientoRepo;
    private final CampaniaRepository campaniaRepo;
    private final LeadRepository leadRepo;
    private final OportunidadRepository oportunidadRepo;
    private final VentaRepository ventaRepo;
    private final AlquilerRepository alquilerRepo;
    private final PasswordEncoder encoder;

    private static final String[] IMG = {
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Pala_excavadora.jpg",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Excavator_14.jpg",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Excavator_02.jpg",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Construction_site_excavator_and_truck.jpg",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Dump_truck_on_haul_road_through_the_prairie_heading_to_berm_construction_area._%2872b620fc-4796-4b38-aa39-87b0022f7ab0%29.JPG",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Bulldozer_on_road_construction.jpg",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Construction_Truck.jpg",
            "https://commons.wikimedia.org/wiki/Special:Redirect/file/Crane_at_construction_site.jpg"
    };

    @Override
    @Transactional
    public void run(String... args) {
        Rol clienteRol = rolRepo.findByNombre("CLIENTE").orElseThrow();
        Usuario admin = usuarioRepo.findByUsername("admin").orElse(null);
        List<Cliente> clientes = crearClientes(clienteRol);
        List<TipoMaquinaria> tipos = crearTiposMaquinaria();
        List<Maquinaria> maquinas = crearMaquinarias(tipos);
        crearTarifas(maquinas);
        List<TipoServicio> tiposServicio = crearTiposServicio();
        List<Empleado> empleados = crearEmpleados();
        List<Proveedor> proveedores = crearProveedores();
        List<CategoriaRepuesto> categorias = crearCategorias();
        List<Repuesto> repuestos = crearRepuestos(categorias);
        List<Almacen> almacenes = crearAlmacenes(empleados);
        List<UbicacionAlmacen> ubicaciones = crearUbicaciones(almacenes);
        crearInventarioMaquinaria(maquinas, ubicaciones);
        crearMovimientos(repuestos, ubicaciones, admin);
        crearSolicitudesCotizacionesContratos(clientes, maquinas, tiposServicio, empleados);
        crearVentasYAlquileres(clientes, maquinas);
        crearMantenimientos(maquinas, empleados, repuestos);
        crearMarketing(clientes, maquinas);
        System.out.println("SIGEM: escenario demo cargado: clientes, maquinaria, solicitudes, cotizaciones, contratos, servicios, RRHH, logistica, mantenimiento y marketing.");
    }

    private List<Cliente> crearClientes(Rol rol) {
        List<Cliente> out = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            final int idx = i;
            String doc = "2061000" + String.format("%04d", i);
            Cliente c = clienteRepo.findByDocumento(doc).orElseGet(() -> {
                Cliente n = new Cliente();
                n.setTipoCliente(idx % 4 == 0 ? "NATURAL" : "JURIDICA");
                n.setRazonSocial(idx % 4 == 0 ? "Cliente Natural Demo " + idx : "Constructora Demo " + idx + " SAC");
                n.setDocumento(doc);
                n.setTelefono("9" + String.format("%08d", 70000000 + idx));
                n.setEmail("cliente" + idx + "@demo.transur.com");
                n.setDireccion("Av. Principal " + (100 + idx) + ", Lima");
                n.setEstado(true);
                n.setFechaRegistro(LocalDateTime.now().minusDays(45L - idx));
                return clienteRepo.save(n);
            });
            String username = "cliente" + String.format("%02d", i);
            Usuario u = usuarioRepo.findByUsername(username).orElseGet(() -> {
                Usuario x = new Usuario();
                x.setUsername(username);
                x.setPassword(encoder.encode("Cliente123*"));
                x.setEmail(c.getEmail());
                x.setNombres("Cliente Demo " + idx);
                x.setApellidos("Portal " + idx);
                x.setRol(rol);
                x.setEstado(true);
                x.setFechaCreacion(LocalDateTime.now().minusDays(40L - idx));
                return usuarioRepo.save(x);
            });
            u.setCliente(c); usuarioRepo.save(u); c.setUsuario(u); clienteRepo.save(c);
            out.add(c);
        }
        return out;
    }

    private List<TipoMaquinaria> crearTiposMaquinaria() {
        String[][] defs = {
                {"EXCAVADORA", "Excavacion y movimiento de tierras."},
                {"RETROEXCAVADORA", "Excavacion, zanjeo y trabajos generales."},
                {"VOLQUETE", "Transporte de agregados y material de obra."},
                {"RODILLO_COMPACTADOR", "Compactacion de suelos y pavimentos."},
                {"CARGADOR_FRONTAL", "Carga y movimiento de materiales."},
                {"MOTONIVELADORA", "Nivelacion y perfilado de terrenos."},
                {"GRUA", "Izaje y manipulacion de cargas."},
                {"CISTERNA", "Riego, abastecimiento y control de polvo."}
        };
        List<TipoMaquinaria> out = new ArrayList<>();
        for (int i=0;i<defs.length;i++) {
            final int k=i;
            out.add(tipoMaquinariaRepo.findByNombre(defs[i][0]).orElseGet(() -> {
                TipoMaquinaria t=new TipoMaquinaria(); t.setNombre(defs[k][0]); t.setDescripcion(defs[k][1]); t.setImageUrl(IMG[k%IMG.length]); return tipoMaquinariaRepo.save(t);
            }));
        }
        return out;
    }

    private List<Maquinaria> crearMaquinarias(List<TipoMaquinaria> tipos) {
        List<Maquinaria> out=new ArrayList<>();
        String[] marcas={"Caterpillar","Komatsu","JCB","Volvo","Hyundai","Liebherr"};
        for(int i=1;i<=30;i++) {
            final int idx = i;
            String code=String.format("M-DEMO-%03d",i);
            Maquinaria m=maquinariaRepo.findByCodigoInterno(code).orElseGet(() -> {
                Maquinaria n=new Maquinaria();
                n.setCodigoInterno(code); n.setTipo(tipos.get((idx-1)%tipos.size())); n.setMarca(marcas[(idx-1)%marcas.length]); n.setModelo("Serie " + (200+idx));
                n.setAnio(2019+(idx%7)); n.setPlaca("T"+String.format("%03d",idx)+"PER"); n.setHorometroActual(BigDecimal.valueOf(800+idx*137));
                n.setEstadoOperativo(idx%10==0?"EN_MANTENIMIENTO_CORRECTIVO":"OPERATIVO");
                n.setDestinoOperativo(idx%7==0?"VENTA":idx%3==0?"SERVICIO":"MULTIPROPOSITO");
                n.setPrecioBaseAlquiler(BigDecimal.valueOf(1800+(idx*75L))); n.setPrecioBaseVenta(BigDecimal.valueOf(85000+(idx*4200L))); n.setImageUrl(IMG[(idx-1)%IMG.length]);
                return maquinariaRepo.save(n);
            }); out.add(m);
        } return out;
    }

    private void crearTarifas(List<Maquinaria> maquinas){
        for(Maquinaria m:maquinas) if(tarifaRepo.findAll().stream().noneMatch(t -> m.getId().equals(t.getMaquinaria().getId()))) {
            for(String tipo:new String[]{"HORA","DIA","SEMANA","MES"}) { Tarifa t=new Tarifa(); t.setMaquinaria(m); t.setTipoTarifa(tipo); BigDecimal p=m.getPrecioBaseAlquiler(); int mult=switch(tipo){case "HORA"->1;case "DIA"->8;case "SEMANA"->45;default->160;}; t.setPrecio(p.multiply(BigDecimal.valueOf(mult)).divide(BigDecimal.valueOf(8),2,java.math.RoundingMode.HALF_UP)); t.setVigenciaInicio(LocalDate.now().minusMonths(3)); tarifaRepo.save(t); }
        }
    }

    private List<TipoServicio> crearTiposServicio(){
        String[][] defs={{"MOVIMIENTO_TIERRA","Excavacion y movimiento de material."},{"TRANSPORTE_CARGA","Transporte de agregados y carga."},{"GRUA","Izaje de equipos y materiales."},{"COMPACTACION","Compactacion de suelos y plataformas."},{"NIVELACION","Nivelacion y perfilado de terrenos."}};
        List<TipoServicio> out=new ArrayList<>(); for(int i=0;i<defs.length;i++){final int k=i; out.add(tipoServicioRepo.findByNombre(defs[i][0]).orElseGet(()->{TipoServicio t=new TipoServicio();t.setNombre(defs[k][0]);t.setDescripcion(defs[k][1]);t.setEstado(true);t.setImageUrl(IMG[(k+2)%IMG.length]);return tipoServicioRepo.save(t);}));} return out;
    }

    private List<Empleado> crearEmpleados(){
        String[] cargos={"OPERADOR","CONDUCTOR","TECNICO","SUPERVISOR"}; List<Empleado> out=new ArrayList<>();
        for(int i=1;i<=20;i++){final int idx=i;String doc="70"+String.format("%07d",i); Empleado e=empleadoRepo.findByDocumento(doc).orElseGet(()->{Empleado n=new Empleado();n.setDocumento(doc);n.setNombres("Empleado Demo "+idx);n.setApellidos("Operaciones "+idx);n.setCargo(cargos[(idx-1)%cargos.length]);n.setTelefono("9"+String.format("%08d",60000000+idx));n.setEmail("empleado"+idx+"@transur.com");n.setFechaIngreso(LocalDate.now().minusDays(300L+idx));n.setEstado(true);return empleadoRepo.save(n);});out.add(e);} return out;
    }

    private List<Proveedor> crearProveedores(){List<Proveedor> out=new ArrayList<>();for(int i=1;i<=15;i++){final int idx=i;String doc="2049000"+String.format("%04d",i);Proveedor p=proveedorRepo.findByDocumento(doc).orElseGet(()->{Proveedor n=new Proveedor();n.setTipoProveedor("JURIDICA");n.setRazonSocial("Proveedor Industrial " + idx + " SAC");n.setDocumento(doc);n.setTelefono("9"+String.format("%08d",50000000+idx));n.setEmail("proveedor"+idx+"@demo.com");n.setDireccion("Av. Industrial "+idx+", Lima");n.setEstado(true);n.setFechaRegistro(LocalDateTime.now().minusDays(idx*8L));return proveedorRepo.save(n);});out.add(p);}return out;}

    private List<CategoriaRepuesto> crearCategorias(){String[] n={"FILTROS","HIDRAULICA","MOTOR","NEUMATICOS","ELECTRICO","TRANSMISION","LUBRICANTES","DESGASTE"};List<CategoriaRepuesto> out=new ArrayList<>();for(String x:n){CategoriaRepuesto c=categoriaRepo.findAll().stream().filter(z->x.equals(z.getNombre())).findFirst().orElseGet(()->{CategoriaRepuesto q=new CategoriaRepuesto();q.setNombre(x);q.setDescripcion("Categoria demo de repuestos "+x);return categoriaRepo.save(q);});out.add(c);}return out;}

    private List<Repuesto> crearRepuestos(List<CategoriaRepuesto> cats){List<Repuesto> out=new ArrayList<>();for(int i=1;i<=50;i++){final int idx=i;String code="REP-DEMO-"+String.format("%03d",i);Repuesto r=repuestoRepo.findByCodigo(code).orElseGet(()->{Repuesto n=new Repuesto();n.setCategoria(cats.get((idx-1)%cats.size()));n.setCodigo(code);n.setNombre("Repuesto demo "+idx);n.setDescripcion("Componente para mantenimiento de maquinaria pesada.");n.setStockActual(20+(idx*3));n.setStockMinimo(5+(idx%6));n.setUnidadMedida(idx%4==0?"LITRO":idx%5==0?"PAR":"UNIDAD");n.setEstado(true);n.setImagenUrl(IMG[(idx+3)%IMG.length]);return repuestoRepo.save(n);});out.add(r);}return out;}

    private List<Almacen> crearAlmacenes(List<Empleado> emp){List<Almacen> out=new ArrayList<>();for(int i=1;i<=3;i++){final int idx=i;String name="ALMACEN DEMO "+i;Almacen a=almacenRepo.findAll().stream().filter(x->name.equals(x.getNombre())).findFirst().orElseGet(()->{Almacen n=new Almacen();n.setNombre(name);n.setDescripcion("Almacen demo para inventario y repuestos.");n.setDireccion("Av. Logistica "+idx+", Lima");n.setResponsable(emp.get(idx-1));n.setEstado(true);return almacenRepo.save(n);});out.add(a);}return out;}

    private List<UbicacionAlmacen> crearUbicaciones(List<Almacen> almacenes){List<UbicacionAlmacen> out=new ArrayList<>();int k=1;for(Almacen a:almacenes)for(int j=1;j<=4;j++){final int jdx=j; final int kdx=k; String code="D"+a.getId()+"-"+String.format("%02d",j);UbicacionAlmacen u=ubicacionRepo.findAll().stream().filter(x->code.equals(x.getCodigo())).findFirst().orElseGet(()->{UbicacionAlmacen n=new UbicacionAlmacen();n.setAlmacen(a);n.setCodigo(code);n.setNombre("Ubicacion "+jdx);n.setDescripcion("Rack y zona demo "+kdx);n.setEstado(true);return ubicacionRepo.save(n);});out.add(u);k++;}return out;}

    private void crearInventarioMaquinaria(List<Maquinaria> maquinas,List<UbicacionAlmacen> ubicaciones){for(int i=0;i<maquinas.size();i++){Maquinaria m=maquinas.get(i);if(inventarioMaquinariaRepo.findAll().stream().noneMatch(x->m.getId().equals(x.getMaquinaria().getId()))){InventarioMaquinaria x=new InventarioMaquinaria();x.setMaquinaria(m);x.setUbicacion(ubicaciones.get(i%ubicaciones.size()));x.setSituacionLogistica("EN_PATIO_CENTRAL");x.setFechaIngreso(LocalDate.now().minusDays(90-i));x.setObservaciones("Registro demo de patio e inventario.");inventarioMaquinariaRepo.save(x);}}}

    private void crearMovimientos(List<Repuesto> repuestos,List<UbicacionAlmacen> ubicaciones,Usuario usuario){if(movimientoRepo.count()>=30)return;for(int i=0;i<35;i++){MovimientoInventario m=new MovimientoInventario();m.setRepuesto(repuestos.get(i%repuestos.size()));m.setUbicacion(ubicaciones.get(i%ubicaciones.size()));m.setTipoMovimiento(i%3==0?"SALIDA":"ENTRADA");m.setCantidad(2+(i%8));m.setMotivo(i%3==0?"Consumo demo de mantenimiento":"Recepcion demo de compra");m.setFecha(LocalDateTime.now().minusDays(i));m.setUsuario(usuario);movimientoRepo.save(m);}}

    private void crearSolicitudesCotizacionesContratos(List<Cliente> clientes,List<Maquinaria> maquinas,List<TipoServicio> tiposServicio,List<Empleado> empleados){
        if(solicitudRepo.count()>=20)return;
        List<Solicitud> solicitudes=new ArrayList<>();
        for(int i=1;i<=20;i++){Solicitud s=new Solicitud();s.setCliente(clientes.get((i-1)%clientes.size()));String tipo=i%3==0?"VENTA":i%3==1?"ALQUILER":"SERVICIO";s.setTipoSolicitud(tipo);s.setDescripcion("Solicitud demo #"+i+" para "+tipo.toLowerCase()+" de maquinaria pesada.");s.setEstado(i%7==0?"RECHAZADA":i%5==0?"PENDIENTE":i%4==0?"CERRADA":"APROBADA");s.setFechaSolicitud(LocalDateTime.now().minusDays(45-i));solicitudes.add(solicitudRepo.save(s));}
        for(int i=0;i<solicitudes.size();i++){Solicitud s=solicitudes.get(i);if(i%7==0)continue;Cotizacion c=new Cotizacion();c.setSolicitud(s);c.setCliente(s.getCliente());c.setNumero(String.format("COT-DEMO-%03d",i+1));c.setFechaEmision(s.getFechaSolicitud().toLocalDate());c.setVigencia(LocalDate.now().plusDays(15));c.setEstado(i%6==0?"ENVIADA":i%5==0?"RECHAZADA":"APROBADA");BigDecimal total=BigDecimal.valueOf(3500+(i*725L));c.setSubtotal(total);c.setImpuestos(total.multiply(BigDecimal.valueOf(.18)).setScale(2));c.setTotal(c.getSubtotal().add(c.getImpuestos()));c=cotizacionRepo.save(c);CotizacionDetalle d=new CotizacionDetalle();d.setCotizacion(c);d.setMaquinaria(maquinas.get(i%maquinas.size()));d.setDescripcion("Servicio / alquiler de maquinaria demo");d.setCantidad(1);d.setPrecioUnitario(total);d.setSubtotal(total);c.getDetalles().add(d);if(i%5!=0){Contrato ct=new Contrato();ct.setCliente(s.getCliente());ct.setCotizacion(c);ct.setNumero(String.format("CTR-DEMO-%03d",i+1));ct.setTipoContrato(s.getTipoSolicitud());ct.setFechaInicio(LocalDate.now().minusDays(20-i));ct.setFechaFin("VENTA".equals(s.getTipoSolicitud())?null:LocalDate.now().plusDays(45+i));ct.setMontoTotal(c.getTotal());ct.setEstado(i%6==0?"VIGENTE":"FINALIZADO");ct=contratoRepo.save(ct);if("SERVICIO".equals(ct.getTipoContrato()))crearServicio(ct,tiposServicio.get(i%tiposServicio.size()),maquinas.get(i%maquinas.size()),empleados.get(i%empleados.size()),i);}}
    }

    private void crearVentasYAlquileres(List<Cliente> clientes, List<Maquinaria> maquinas) {
        // Ventas demo: contratos VENTA -> entidad Venta + detalles.
        List<Contrato> contratosVenta = contratoRepo.findAll().stream()
                .filter(c -> "VENTA".equals(c.getTipoContrato()))
                .filter(c -> c.getNumero() != null && c.getNumero().startsWith("CTR-DEMO-"))
                .toList();

        int ventasCreadas = 0;
        for (Contrato ct : contratosVenta) {
            if (ventasCreadas >= 6) break;
            String numero = String.format("VTA-DEMO-%03d", ventasCreadas + 1);
            if (ventaRepo.findByNumero(numero).isPresent()) {
                ventasCreadas++;
                continue;
            }

            Maquinaria maquina = maquinas.stream()
                    .filter(m -> "OPERATIVO".equals(m.getEstadoOperativo()))
                    .filter(m -> !"ALQUILADA".equals(m.getEstadoOperativo()))
                    .findFirst()
                    .orElse(null);
            if (maquina == null) break;

            BigDecimal precio = maquina.getPrecioBaseVenta() != null
                    ? maquina.getPrecioBaseVenta().multiply(BigDecimal.valueOf(0.88))
                    : BigDecimal.valueOf(180000 + ventasCreadas * 25000L);
            precio = precio.setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal impuesto = precio.multiply(BigDecimal.valueOf(0.18)).setScale(2, java.math.RoundingMode.HALF_UP);

            Venta venta = new Venta();
            venta.setContrato(ct);
            venta.setNumero(numero);
            venta.setFechaVenta(LocalDate.now().minusDays(ventasCreadas * 4L));
            venta.setSubtotal(precio);
            venta.setImpuestos(impuesto);
            venta.setTotalFinal(precio.add(impuesto));
            venta.setEstado(ventasCreadas % 3 == 0 ? "ENTREGADA" : ventasCreadas % 3 == 1 ? "GARANTIA_ACTIVA" : "COMPLETADA");

            VentaDetalle detalle = new VentaDetalle();
            detalle.setVenta(venta);
            detalle.setMaquinaria(maquina);
            detalle.setPrecioFinal(precio);
            detalle.setCantidad(1);
            detalle.setSubtotal(precio);
            List<VentaDetalle> detalles = new ArrayList<>();
            detalles.add(detalle);
            venta.setDetalles(detalles);

            ventaRepo.save(venta);
            maquina.setEstadoOperativo("VENDIDO");
            maquinariaRepo.save(maquina);
            ct.setEstado("FINALIZADO");
            contratoRepo.save(ct);
            ventasCreadas++;
        }

        // Alquileres demo: contratos ALQUILER -> entidad Alquiler + detalles.
        List<Contrato> contratosAlquiler = contratoRepo.findAll().stream()
                .filter(c -> "ALQUILER".equals(c.getTipoContrato()))
                .filter(c -> c.getNumero() != null && c.getNumero().startsWith("CTR-DEMO-"))
                .toList();

        int alquileresCreados = 0;
        for (Contrato ct : contratosAlquiler) {
            if (alquileresCreados >= 8) break;
            if (alquilerRepo.findByContratoId(ct.getId()).isPresent()) {
                alquileresCreados++;
                continue;
            }

            Maquinaria maquina = maquinas.stream()
                    .filter(m -> "OPERATIVO".equals(m.getEstadoOperativo()))
                    .findFirst()
                    .orElse(null);
            if (maquina == null) break;

            Tarifa tarifa = tarifaRepo.findAll().stream()
                    .filter(t -> t.getMaquinaria() != null && maquina.getId().equals(t.getMaquinaria().getId()))
                    .filter(t -> "DIA".equals(t.getTipoTarifa()))
                    .findFirst()
                    .orElse(null);
            if (tarifa == null) continue;

            int unidades = 2 + (alquileresCreados % 6);
            BigDecimal subtotal = tarifa.getPrecio().multiply(BigDecimal.valueOf(unidades));

            Alquiler alquiler = new Alquiler();
            alquiler.setContrato(ct);
            alquiler.setFechaInicioReal(LocalDateTime.now().minusDays(alquileresCreados * 3L));
            boolean finalizado = alquileresCreados % 4 == 0;
            alquiler.setFechaFinReal(finalizado ? LocalDateTime.now().minusDays(alquileresCreados + 1L) : null);
            alquiler.setEstado(finalizado ? "FINALIZADO" : alquileresCreados % 3 == 0 ? "PROGRAMADO" : "EN_CURSO");
            alquiler.setObservaciones(finalizado
                    ? "Alquiler demo finalizado con devolución y revisión de la maquinaria."
                    : "Alquiler demo activo para presentación comercial.");

            AlquilerDetalle detalle = new AlquilerDetalle();
            detalle.setAlquiler(alquiler);
            detalle.setMaquinaria(maquina);
            detalle.setTarifa(tarifa);
            detalle.setUnidadesTiempo(unidades);
            detalle.setPrecioUnitario(tarifa.getPrecio());
            detalle.setSubtotal(subtotal);
            List<AlquilerDetalle> detalles = new ArrayList<>();
            detalles.add(detalle);
            alquiler.setDetalles(detalles);

            alquilerRepo.save(alquiler);
            if (!finalizado) {
                maquina.setEstadoOperativo("ALQUILADA");
                maquinariaRepo.save(maquina);
            }
            ct.setEstado(finalizado ? "FINALIZADO" : "VIGENTE");
            contratoRepo.save(ct);
            alquileresCreados++;
        }
    }

    private void crearServicio(Contrato ct,TipoServicio ts,Maquinaria m,Empleado e,int i){Servicio s=new Servicio();s.setContrato(ct);s.setTipoServicio(ts);s.setOrigen("Lima - Planta "+(i+1));s.setDestino("Obra demo "+(i+1));s.setFechaProgramada(LocalDateTime.now().plusDays(i%12-3));s.setEstado(i%4==0?"FINALIZADO":i%3==0?"EN_CURSO":"ASIGNADO");s.setObservaciones("Servicio demo generado para presentacion.");s.setFotoChecklistUrl(i%4==0?IMG[i%IMG.length]:null);s=servicioRepo.save(s);Asignacion a=new Asignacion();a.setServicio(s);a.setMaquinaria(m);a.setEmpleado(e);a.setFechaAsignacion(LocalDateTime.now().minusDays(i%15));a.setEstado(s.getEstado().equals("FINALIZADO")?"COMPLETADA":"ACTIVA");asignacionRepo.save(a);if(i%4==1){Incidencia in=new Incidencia();in.setServicio(s);in.setMaquinaria(m);in.setTipo("MECANICA");in.setDescripcion("Incidencia demo: revision preventiva requerida.");in.setFecha(LocalDateTime.now().minusDays(i%10));in.setEstado("ABIERTA");in.setFotoEvidenciaUrl(IMG[(i+1)%IMG.length]);incidenciaRepo.save(in);}}

    private void crearMantenimientos(List<Maquinaria> maquinas,List<Empleado> empleados,List<Repuesto> repuestos){if(mantenimientoRepo.count()>=20)return;for(int i=0;i<20;i++){Mantenimiento m=new Mantenimiento();m.setMaquinaria(maquinas.get((i+2)%maquinas.size()));m.setEmpleado(empleados.get(i%empleados.size()));m.setTipoMantenimiento(i%3==0?"CORRECTIVO":"PREVENTIVO");m.setDescripcion(i%3==0?"Cambio de componentes y correccion de falla demo.":"Mantenimiento preventivo programado demo.");m.setFechaProgramada(LocalDate.now().minusDays(30-i));m.setFechaInicio(LocalDate.now().minusDays(25-i));m.setFechaFin(i%5==0?null:LocalDate.now().minusDays(20-i));m.setCosto(BigDecimal.valueOf(450+(i*125L)));m.setEstado(i%5==0?"EN_PROCESO":"COMPLETADO");m.setObservaciones("Registro demo de mantenimiento.");mantenimientoRepo.save(m);}}

    private void crearMarketing(List<Cliente> clientes,List<Maquinaria> maquinas){if(campaniaRepo.count()>=4)return;List<Campania> cs=new ArrayList<>();for(int i=1;i<=4;i++){Campania c=new Campania();c.setNombre("Campaña Demo "+i);c.setDescripcion("Campaña comercial para maquinaria y servicios.");c.setFechaInicio(LocalDate.now().minusDays(60));c.setFechaFin(LocalDate.now().plusDays(30));c.setPresupuesto(BigDecimal.valueOf(5000+i*1200L));c.setEstado(i==4?"PAUSADA":"ACTIVA");c.setBannerUrl(IMG[(i+1)%IMG.length]);c.setLeadsGenerados(8+i);cs.add(campaniaRepo.save(c));}for(int i=1;i<=20;i++){Lead l=new Lead();l.setCampania(cs.get((i-1)%cs.size()));l.setNombres("Lead Demo "+i);l.setEmpresa("Empresa Prospecto "+i);l.setTelefono("9"+String.format("%08d",40000000+i));l.setEmail("lead"+i+"@demo.com");l.setOrigen(i%4==0?"REFERIDO":i%3==0?"LINKEDIN":"WEB");l.setEstado(i%5==0?"PERDIDO":i%3==0?"CALIFICADO":"CONTACTADO");l.setFechaRegistro(LocalDateTime.now().minusDays(i));l.setServicio(maquinas.get(i%maquinas.size()));l=leadRepo.save(l);Oportunidad o=new Oportunidad();o.setLead(l);o.setCliente(clientes.get(i%clientes.size()));o.setDescripcion("Oportunidad comercial demo "+i);o.setMontoEstimado(BigDecimal.valueOf(10000+i*850L));o.setProbabilidad(20+(i%8)*10);o.setEstado(i%5==0?"CERRADO":i%3==0?"NEGOCIACION":"PROPUESTA");o.setFechaCreacion(LocalDateTime.now().minusDays(i));oportunidadRepo.save(o);}}
}
