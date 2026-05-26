package com.medicamentos.domain.model;

import com.medicamentos.domain.enums.TipoDiagnostico;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "atenciones")
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "fecha_evaluacion", nullable = false)
    private LocalDate fechaEvaluacion;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Column(columnDefinition = "text")
    private String antecedentes;

    @Column(columnDefinition = "text")
    private String inmunizaciones;

    @Column(name = "signos_vitales", columnDefinition = "text")
    private String signosVitales;

    @Column(name = "examen_fisico", columnDefinition = "text")
    private String examenFisico;

    @Column(name = "laboratorio", columnDefinition = "text")
    private String laboratorio;

    @Column(name = "diagnostico_1", length = 150)
    private String diagnostico1;

    @Column(name = "cie10_1", length = 15)
    private String cie101;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_diagnostico_1", length = 20)
    private TipoDiagnostico tipoDiagnostico1;

    @Column(name = "diagnostico_2", length = 150)
    private String diagnostico2;

    @Column(name = "cie10_2", length = 15)
    private String cie102;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_diagnostico_2", length = 20)
    private TipoDiagnostico tipoDiagnostico2;

    @Column(name = "diagnostico_3", length = 150)
    private String diagnostico3;

    @Column(name = "cie10_3", length = 15)
    private String cie103;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_diagnostico_3", length = 20)
    private TipoDiagnostico tipoDiagnostico3;

    @Column(length = 80)
    private String conclusion;

    @Column(length = 120)
    private String derivacion;

    @Column(name = "observaciones", columnDefinition = "text")
    private String observaciones;

    @Column(name = "usuario_registro", nullable = false, length = 80)
    private String usuarioRegistro;

    @OneToMany(mappedBy = "atencion")
    private List<ConsumoMedicamento> consumos = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
