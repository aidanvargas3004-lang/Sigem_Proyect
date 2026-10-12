package com.transur.sigem.compras.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String tipoProveedor; // NATURAL o JURIDICA (Validado por CHECK en BD)

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 30)
    private String documento; // RUC o DNI del proveedor

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}