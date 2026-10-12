package com.transur.sigem.comercial.entity;

import com.transur.sigem.cliente.entity.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // TIPO DE SOLICITUD: Controlado por CHECK en BD
    @Column(name = "tipo_solicitud", nullable = false, length = 30)
    private String tipoSolicitud;
    // VENTA, ALQUILER, SERVICIO

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";
    // PENDIENTE, EN_COTIZACION, APROBADA, RECHAZADA, CERRADA

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();
}