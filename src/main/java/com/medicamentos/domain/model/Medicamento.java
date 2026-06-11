package com.medicamentos.domain.model;

import com.medicamentos.domain.enums.TipoProducto;
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

import java.math.BigDecimal;
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

    @Column(name = "nombre", nullable = false, length = 200,
            columnDefinition = "VARCHAR(200) DEFAULT 'SIN NOMBRE'")
    private String nombre;

    @Column(name = "registro_sanitario", length = 30)
    private String registroSanitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_producto", length = 20)
    private TipoProducto tipoProducto;

    @Column(name = "presentacion_frasco", length = 120)
    private String presentacion;

    @Column(name = "fabricante", length = 150)
    private String fabricante;

    @Column(name = "pais_fabricacion", length = 80)
    private String paisFabricacion;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "stock_minimo")
    private Integer stockMinimo = 0;

    @Column(name = "codigo_sismed", unique = true, length = 20)
    private String codigoSismed;

    @Column(name = "descripcion_sismed", length = 300)
    private String descripcionSismed;

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
