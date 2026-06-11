package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.AuditLog;
import com.medicamentos.dto.response.AuditLogDTO;
import com.medicamentos.repository.AuditLogRepository;
import com.medicamentos.repository.UsuarioRepository;
import com.medicamentos.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String accion, String modulo, String detalle) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return;

            String username = auth.getName();
            usuarioRepository.findByUsername(username).ifPresent(u -> {
                AuditLog entry = new AuditLog();
                entry.setTimestamp(LocalDateTime.now());
                entry.setUserEmail(u.getEmail());
                entry.setUserRol(u.getRol().name());
                entry.setAccion(accion);
                entry.setModulo(modulo);
                entry.setDetalle(detalle);
                auditLogRepository.save(entry);
            });
        } catch (Exception e) {
            log.warn("Error registrando audit log [{}:{}]: {}", modulo, accion, e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogin(String email, String rol, boolean exitoso) {
        try {
            AuditLog entry = new AuditLog();
            entry.setTimestamp(LocalDateTime.now());
            entry.setUserEmail(email);
            entry.setUserRol(rol);
            entry.setAccion(exitoso ? "LOGIN_EXITOSO" : "LOGIN_FALLIDO");
            entry.setModulo("Seguridad");
            entry.setDetalle(exitoso
                    ? "Inicio de sesión exitoso: " + email
                    : "Intento de inicio de sesión fallido: " + email);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Error registrando audit login: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getLogs(String accion, String modulo, String email,
                                      LocalDate desde, LocalDate hasta, Pageable pageable) {
        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;
        String accionParam = (accion != null && !accion.isBlank()) ? accion : null;
        String moduloParam = (modulo != null && !modulo.isBlank()) ? modulo : null;
        String emailParam = (email != null && !email.isBlank()) ? email : null;

        return auditLogRepository.findFiltered(accionParam, moduloParam, emailParam, desdeTs, hastaTs, pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getRecent(int limit) {
        return auditLogRepository.findTop10ByOrderByTimestampDesc()
                .stream().limit(limit).map(this::toDTO).toList();
    }

    private AuditLogDTO toDTO(AuditLog a) {
        return new AuditLogDTO(a.getId(), a.getTimestamp(), a.getUserEmail(),
                a.getUserRol(), a.getAccion(), a.getModulo(), a.getDetalle());
    }
}
