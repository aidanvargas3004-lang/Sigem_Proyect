package com.transur.sigem.marketing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "campanias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Campania {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(precision = 12, scale = 2)
    private BigDecimal presupuesto;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVA"; // ACTIVA, FINALIZADA, PAUSADA

    // RELACIÓN: Una campaña tiene muchos leads
    @OneToMany(mappedBy = "campania")
    private List<Lead> leads;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl; // Banner promocional de la campaña
    @Column(name = "leads_generados")
    private Integer leadsGenerados;
}