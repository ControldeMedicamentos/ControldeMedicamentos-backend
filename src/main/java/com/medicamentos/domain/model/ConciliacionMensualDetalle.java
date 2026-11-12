package com.medicamentos.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conciliaciones_mensuales_detalle", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conciliacion_medicamento", columnNames = {"conciliacion_id", "medicamento_id"})
})
public class ConciliacionMensualDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conciliacion_id", nullable = false)
    private ConciliacionMensual conciliacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(name = "stock_sistema", nullable = false)
    private Integer stockSistema;

    @Column(name = "stock_fisico", nullable = false)
    private Integer stockFisico;

    @Column(nullable = false)
    private Integer diferencia;
}
