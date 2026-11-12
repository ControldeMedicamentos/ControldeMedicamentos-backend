package com.medicamentos.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movimientos_inventario_lotes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_movimiento_inventario_lote", columnNames = {"movimiento_id", "inventario_id"})
}, indexes = {
        @Index(name = "idx_movimiento_lote_movimiento", columnList = "movimiento_id"),
        @Index(name = "idx_movimiento_lote_inventario", columnList = "inventario_id")
})
public class MovimientoInventarioLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movimiento_id", nullable = false)
    private MovimientoInventario movimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "stock_antes", nullable = false)
    private Integer stockAntes;

    @Column(name = "stock_despues", nullable = false)
    private Integer stockDespues;
}
