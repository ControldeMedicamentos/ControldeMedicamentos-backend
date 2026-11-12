package com.medicamentos.security;

import com.medicamentos.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class AuditRequestFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditLogService auditLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException exception) {
            logIfRequired(request, response, true);
            throw exception;
        }
        logIfRequired(request, response, false);
    }

    private void logIfRequired(HttpServletRequest request, HttpServletResponse response, boolean unhandledFailure) {
        if (Boolean.TRUE.equals(request.getAttribute(AuditLogService.REQUEST_AUDITED_ATTRIBUTE))) return;

        int status = response.getStatus();
        boolean accessFailure = status == HttpServletResponse.SC_UNAUTHORIZED
                || status == HttpServletResponse.SC_FORBIDDEN;
        if (!MUTATING_METHODS.contains(request.getMethod()) && !accessFailure && !unhandledFailure) return;

        String resultado = accessFailure ? "DENEGADO"
                : (unhandledFailure || status >= 400 ? "FALLIDO" : "EXITOSO");
        String accion = status == HttpServletResponse.SC_FORBIDDEN ? "ACCESO_DENEGADO"
                : status == HttpServletResponse.SC_UNAUTHORIZED ? "ACCESO_NO_AUTENTICADO"
                : resultado.equals("EXITOSO") ? "OPERACION_HTTP" : "OPERACION_FALLIDA";
        String path = request.getRequestURI();
        String detalle = request.getMethod() + " " + path + " — HTTP "
                + (unhandledFailure && status < 400 ? 500 : status);

        auditLogService.logRequestEvent(accion, resolveModule(path), detalle, resultado);
    }

    private String resolveModule(String path) {
        if (path.contains("/auth")) return "Seguridad";
        if (path.contains("/admin/audit-logs")) return "Auditoría";
        if (path.contains("/admin/empleados")) return "Empleados";
        if (path.contains("/admin/roles")) return "Roles";
        if (path.contains("/patients")) return "Pacientes";
        if (path.contains("/appointments")) return "Atenciones";
        if (path.contains("/medicines")) return "Medicamentos";
        if (path.contains("/inventory")) return "Inventario";
        if (path.contains("/reports")) return "Reportes";
        return "Sistema";
    }
}
