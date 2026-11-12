package com.medicamentos.security;

import com.medicamentos.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuditRequestFilterTest {

    private final AuditLogService auditLogService = mock(AuditLogService.class);
    private final AuditRequestFilter filter = new AuditRequestFilter(auditLogService);

    @Test
    void recordsSuccessfulMutationWhenControllerDidNotCreateAnEvent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/inventory-movements");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(201));

        verify(auditLogService).logRequestEvent(
                "OPERACION_HTTP", "Inventario", "POST /api/inventory-movements — HTTP 201", "EXITOSO");
    }

    @Test
    void recordsDeniedRead() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/audit-logs");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> ((MockHttpServletResponse) res).setStatus(403));

        verify(auditLogService).logRequestEvent(
                "ACCESO_DENEGADO", "Auditoría", "GET /api/admin/audit-logs — HTTP 403", "DENEGADO");
    }

    @Test
    void avoidsDuplicateWhenBusinessEventWasRecorded() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/auth/change-password");
        request.setAttribute(AuditLogService.REQUEST_AUDITED_ATTRIBUTE, Boolean.TRUE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        verify(auditLogService, never()).logRequestEvent(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}
