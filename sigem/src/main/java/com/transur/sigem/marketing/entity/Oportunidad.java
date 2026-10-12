package com.transur.sigem.marketing.entity;

import com.transur.sigem.cliente.entity.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "oportunidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Oportunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN OPCIONAL: Puede existir sin lead previo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    // RELACIÓN OPCIONAL: Puede existir sin cliente formal aún
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "monto_estimado", precision = 12, scale = 2)
    private BigDecimal montoEstimado;

    // VALIDACIÓN: CHECK (probabilidad BETWEEN 0 AND 100)
    // Se valida en BD, pero podemos agregar lógica en Service también
    @Column
    private Integer probabilidad;

    @Column(nullable = false, length = 30)
    private String estado = "PROSPECTO"; // PROSPECTO, PROPUESTA, NEGOCIACION, CERRADO

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}