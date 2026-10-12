package com.transur.sigem.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre; // EJ: ADMIN, MARKETING, CLIENTE

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean estado = true;
}