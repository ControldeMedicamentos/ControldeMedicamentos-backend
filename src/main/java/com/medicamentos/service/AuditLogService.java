package com.medicamentos.service;

import com.medicamentos.dto.response.AuditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AuditLogService {

    void log(String accion, String modulo, String detalle);

    void logLogin(String email, String rol, boolean exitoso);

    Page<AuditLogDTO> getLogs(String accion, String modulo, String email,
                               LocalDate desde, LocalDate hasta, Pageable pageable);

    List<AuditLogDTO> getRecent(int limit);
}
