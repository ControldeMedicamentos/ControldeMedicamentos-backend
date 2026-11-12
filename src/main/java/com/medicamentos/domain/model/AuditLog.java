package com.medicamentos.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_logs_user_email", columnList = "user_email"),
        @Index(name = "idx_audit_logs_ip_address", columnList = "ip_address"),
        @Index(name = "idx_audit_logs_resultado", columnList = "resultado")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_email", length = 120)
    private String userEmail;

    @Column(name = "user_rol", length = 30)
    private String userRol;

    @Column(nullable = false, length = 60)
    private String accion;

    @Column(nullable = false, length = 50)
    private String modulo;

    @Column(columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_path", length = 300)
    private String requestPath;

    @Column(length = 20)
    private String resultado;

    @Column(name = "entidad_tipo", length = 60)
    private String entidadTipo;

    @Column(name = "entidad_id", length = 80)
    private String entidadId;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
