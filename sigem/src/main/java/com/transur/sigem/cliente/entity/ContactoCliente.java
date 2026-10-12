package com.transur.sigem.cliente.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contactos_cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN: Muchos contactos pertenecen a un solo cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(length = 100)
    private String cargo;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;
}