package com.transur.sigem.logistica.entity;

import com.transur.sigem.rrhh.entity.Empleado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "almacenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // EJ: ALMACEN CENTRAL, PATIO SUR

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 255)
    private String direccion;

    // RELACIÓN OPCIONAL: Un almacén puede no tener responsable asignado aún
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Empleado responsable;

    @Column(nullable = false)
    private Boolean estado = true;

    // RELACIÓN: Un almacén tiene muchas ubicaciones físicas
    @OneToMany(mappedBy = "almacen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UbicacionAlmacen> ubicaciones;
}