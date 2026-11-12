package com.medicamentos.domain.model;

import com.medicamentos.domain.enums.ResultadoConciliacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conciliaciones_mensuales", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conciliacion_periodo", columnNames = "periodo")
})
public class ConciliacionMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 6)
    private String periodo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ResultadoConciliacion resultado;

    @Column(columnDefinition = "text")
    private String observacion;

    @Column(name = "usuario_registro", nullable = false, length = 80)
    private String usuarioRegistro;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "conciliacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConciliacionMensualDetalle> detalles = new ArrayList<>();

    public void reemplazarDetalles(List<ConciliacionMensualDetalle> nuevosDetalles) {
        var medicamentosNuevos = nuevosDetalles.stream()
                .map(detalle -> detalle.getMedicamento().getId())
                .collect(java.util.stream.Collectors.toSet());
        detalles.removeIf(detalle -> !medicamentosNuevos.contains(detalle.getMedicamento().getId()));
        nuevosDetalles.forEach(detalle -> {
            ConciliacionMensualDetalle existente = detalles.stream()
                    .filter(actual -> actual.getMedicamento().getId().equals(detalle.getMedicamento().getId()))
                    .findFirst()
                    .orElse(null);
            if (existente != null) {
                existente.setStockSistema(detalle.getStockSistema());
                existente.setStockFisico(detalle.getStockFisico());
                existente.setDiferencia(detalle.getDiferencia());
            } else {
                detalle.setConciliacion(this);
                detalles.add(detalle);
            }
        });
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
