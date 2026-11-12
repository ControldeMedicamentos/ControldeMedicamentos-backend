package com.medicamentos.service;

import com.medicamentos.dto.response.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AuditLogService {

    String REQUEST_AUDITED_ATTRIBUTE = AuditLogService.class.getName() + ".RECORDED";

    void log(String accion, String modulo, String detalle);

    void log(String accion, String modulo, String detalle, String entidadTipo, String entidadId);

    void logLogin(String email, String rol, boolean exitoso);

    void logRequestEvent(String accion, String modulo, String detalle, String resultado);

    Page<AuditLogDTO> getLogs(String accion, String modulo, String email, String ipAddress, String resultado,
                              LocalDate desde, LocalDate hasta, Pageable pageable);

    List<AuditLogDTO> getRecent(int limit);
}
