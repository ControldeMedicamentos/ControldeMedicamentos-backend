package com.medicamentos.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "medicamentos")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_sismed", nullable = false, unique = true, length = 20)
    private String codigoSismed;

    @Column(name = "codigo_siga", length = 30)
    private String codigoSiga;

    @Column(name = "descripcion_sismed", nullable = false, length = 300)
    private String descripcionSismed;

    @Column(name = "presentacion_frasco", length = 80)
    private String presentacionFrasco;

    @Column(name = "descripcion_corta", length = 120)
    private String descripcionCorta;

    @Column(nullable = false)
    private Integer conversion = 1;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "medicamento")
    private List<Inventario> inventarios = new ArrayList<>();

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
