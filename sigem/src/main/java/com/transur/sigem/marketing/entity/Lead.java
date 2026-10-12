package com.transur.sigem.marketing.entity;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.operaciones.entity.Servicio;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN OPCIONAL: Un lead puede no pertenecer a ninguna campaña
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campania_id")
    private Campania campania;

    @Column(nullable = false, length = 150)
    private String nombres;

    @Column(length = 150)
    private String empresa;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String origen; // WEB, REFERIDO, LINKEDIN, TELEFONO

    @Column(nullable = false, length = 30)
    private String estado = "NUEVO"; // NUEVO, CONTACTADO, CALIFICADO, PERDIDO

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "foto_contacto_url", length = 500)
    private String fotoContactoUrl; // Foto del lead/contacto principal

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id") // Nombre de la columna en BD
    private Maquinaria servicio; // O Maquinaria, según tu modelo
}