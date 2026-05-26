package com.medicamentos.domain.model;

import com.medicamentos.domain.enums.Sexo;
import com.medicamentos.domain.enums.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
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
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 25)
    private TipoDocumento tipoDocumento = TipoDocumento.DNI;

    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String nroDocumento;

    @Column(name = "nombres_apellidos", nullable = false, length = 150)
    private String nombresApellidos;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    @Column(name = "carrera_area", length = 150)
    private String carreraArea;

    @Column(name = "ciclo_academico", length = 30)
    private String cicloAcademico;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

    @OneToMany(mappedBy = "paciente")
    private List<Atencion> atenciones = new ArrayList<>();

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
