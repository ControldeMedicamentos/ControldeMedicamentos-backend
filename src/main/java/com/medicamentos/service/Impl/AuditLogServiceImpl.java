package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.AuditLog;
import com.medicamentos.dto.response.AuditLogDTO;
import com.medicamentos.repository.AuditLogRepository;
import com.medicamentos.repository.UsuarioRepository;
import com.medicamentos.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
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
    private final ObjectProvider<HttpServletRequest> requestProvider;

    @Value("${app.audit.trust-forwarded-headers:false}")
    private boolean trustForwardedHeaders;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String accion, String modulo, String detalle) {
        log(accion, modulo, detalle, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String accion, String modulo, String detalle, String entidadTipo, String entidadId) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return;

            var usuario = usuarioRepository.findByUsername(auth.getName()).orElse(null);
            String email = usuario != null ? usuario.getEmail() : auth.getName();
            String rol = usuario != null ? usuario.getRol().name() : resolveRole(auth);
            saveEntry(email, rol, accion, modulo, detalle, "EXITOSO", entidadTipo, entidadId);
        } catch (Exception e) {
            log.warn("Error registrando audit log [{}:{}]: {}", modulo, accion, e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogin(String email, String rol, boolean exitoso) {
        try {
            saveEntry(email, rol, exitoso ? "LOGIN_EXITOSO" : "LOGIN_FALLIDO", "Seguridad",
                    exitoso ? "Inicio de sesión exitoso" : "Intento de inicio de sesión fallido",
                    exitoso ? "EXITOSO" : "FALLIDO", "USUARIO", null);
        } catch (Exception e) {
            log.warn("Error registrando audit login: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequestEvent(String accion, String modulo, String detalle, String resultado) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = "ANONIMO";
            String rol = "SIN_ROL";
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                var usuario = usuarioRepository.findByUsername(auth.getName()).orElse(null);
                email = usuario != null ? usuario.getEmail() : auth.getName();
                rol = usuario != null ? usuario.getRol().name() : resolveRole(auth);
            }
            saveEntry(email, rol, accion, modulo, detalle, resultado, null, null);
        } catch (Exception e) {
            log.warn("Error registrando resultado HTTP [{}:{}]: {}", modulo, accion, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getLogs(String accion, String modulo, String email, String ipAddress, String resultado,
                                      LocalDate desde, LocalDate hasta, Pageable pageable) {
        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(23, 59, 59) : null;
        String accionParam = (accion != null && !accion.isBlank()) ? accion : null;
        String moduloParam = (modulo != null && !modulo.isBlank()) ? modulo : null;
        String emailParam = (email != null && !email.isBlank()) ? email : null;
        String ipAddressParam = (ipAddress != null && !ipAddress.isBlank()) ? ipAddress : null;
        String resultadoParam = (resultado != null && !resultado.isBlank()) ? resultado : null;

        return auditLogRepository.findFiltered(accionParam, moduloParam, emailParam, ipAddressParam,
                        resultadoParam, desdeTs, hastaTs, pageable)
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
                a.getUserRol(), a.getAccion(), a.getModulo(), a.getDetalle(),
                a.getIpAddress(), a.getRequestMethod(), a.getRequestPath(),
                a.getResultado() != null ? a.getResultado() : "EXITOSO",
                a.getEntidadTipo(), a.getEntidadId(), a.getUserAgent());
    }

    private void saveEntry(String email, String rol, String accion, String modulo, String detalle,
                           String resultado, String entidadTipo, String entidadId) {
        AuditLog entry = new AuditLog();
        entry.setTimestamp(LocalDateTime.now());
        entry.setUserEmail(truncate(email, 120));
        entry.setUserRol(truncate(rol, 30));
        entry.setAccion(truncate(accion, 60));
        entry.setModulo(truncate(modulo, 50));
        entry.setDetalle(detalle);
        entry.setResultado(truncate(resultado, 20));
        entry.setEntidadTipo(truncate(entidadTipo, 60));
        entry.setEntidadId(truncate(entidadId, 80));
        applyRequestMetadata(entry);
        auditLogRepository.save(entry);
        markRequestAudited();
    }

    private void applyRequestMetadata(AuditLog entry) {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null) return;
        entry.setIpAddress(truncate(resolveClientIp(request), 64));
        entry.setRequestMethod(truncate(request.getMethod(), 10));
        entry.setRequestPath(truncate(request.getRequestURI(), 300));
        entry.setUserAgent(truncate(request.getHeader("User-Agent"), 500));
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (trustForwardedHeaders) {
            String forwarded = firstHeaderValue(request.getHeader("Forwarded"));
            if (forwarded != null) {
                for (String part : forwarded.split(";")) {
                    String trimmed = part.trim();
                    if (trimmed.regionMatches(true, 0, "for=", 0, 4)) {
                        return normalizeIp(trimmed.substring(4));
                    }
                }
            }
            String forwardedFor = firstHeaderValue(request.getHeader("X-Forwarded-For"));
            if (forwardedFor != null) return normalizeIp(forwardedFor);
            String realIp = firstHeaderValue(request.getHeader("X-Real-IP"));
            if (realIp != null) return normalizeIp(realIp);
        }
        return normalizeIp(request.getRemoteAddr());
    }

    private String firstHeaderValue(String value) {
        if (value == null || value.isBlank()) return null;
        return value.split(",", 2)[0].trim();
    }

    private String normalizeIp(String value) {
        if (value == null) return null;
        String normalized = value.trim().replace("\"", "");
        if (normalized.startsWith("[") && normalized.contains("]")) {
            return normalized.substring(1, normalized.indexOf(']'));
        }
        return normalized;
    }

    private String resolveRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElse("DESCONOCIDO");
    }

    private void markRequestAudited() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request != null) request.setAttribute(REQUEST_AUDITED_ATTRIBUTE, Boolean.TRUE);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }
}
