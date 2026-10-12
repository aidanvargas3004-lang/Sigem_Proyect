package com.transur.sigem.maquinaria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipos_maquinaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoMaquinaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // EJ: EXCAVADORA, VOLQUETE, CISTERNA

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_url", length = 500)
    private String imageUrl;
}