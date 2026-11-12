package com.medicamentos.dto.response;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        LocalDateTime timestamp,
        String userEmail,
        String userRol,
        String accion,
        String modulo,
        String detalle,
        String ipAddress,
        String requestMethod,
        String requestPath,
        String resultado,
        String entidadTipo,
        String entidadId,
        String userAgent
) {}
