package com.medicamentos.domain.model;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.enums.TipoMovimientoInventario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atencion_id")
    private Atencion atencion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private TipoMovimientoInventario tipoMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_consumo", length = 30)
    private TipoConsumo tipoConsumo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(length = 6)
    private String periodo;

    @Column(columnDefinition = "text")
    private String observacion;

    @Column(name = "usuario_registro", nullable = false, length = 80)
    private String usuarioRegistro;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
