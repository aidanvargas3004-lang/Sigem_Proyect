package com.transur.sigem.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String codigo; // EJ: VER_DASHBOARD, CREAR_SOLICITUD

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String modulo; // EJ: MARKETING, VENTAS, OPERACIONES

    @Column(length = 150)
    private String ruta; // Ruta relativa para el sidebar dinámico

    @Column(length = 100)
    private String icono; // Nombre del icono (Bootstrap Icons / FontAwesome)

    @Column(nullable = false)
    private Integer orden = 0; // Orden de visualización en el menú

    @Column(nullable = false)
    private Boolean estado = true;
}