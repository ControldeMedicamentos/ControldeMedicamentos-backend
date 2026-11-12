package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.AuditLog;
import com.medicamentos.repository.AuditLogRepository;
import com.medicamentos.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogServiceImplTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usesRemoteAddressWhenForwardedHeadersAreNotTrusted() {
        MockHttpServletRequest request = request("203.0.113.10");
        request.addHeader("X-Forwarded-For", "198.51.100.8, 10.0.0.2");
        AuditLogServiceImpl service = service(request, false);
        authenticate();

        service.log("EDITAR_ATENCION", "Atenciones", "Datos clínicos actualizados", "ATENCION", "15");

        var captor = org.mockito.ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("203.0.113.10");
        assertThat(captor.getValue().getResultado()).isEqualTo("EXITOSO");
        assertThat(captor.getValue().getEntidadTipo()).isEqualTo("ATENCION");
        assertThat(captor.getValue().getEntidadId()).isEqualTo("15");
    }

    @Test
    void usesFirstForwardedAddressOnlyWhenProxyIsTrusted() {
        MockHttpServletRequest request = request("10.0.0.2");
        request.addHeader("X-Forwarded-For", "198.51.100.8, 10.0.0.2");
        AuditLogServiceImpl service = service(request, true);
        authenticate();

        service.logRequestEvent("ACCESO_DENEGADO", "Inventario", "GET /inventory", "DENEGADO");

        var captor = org.mockito.ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        assertThat(captor.getValue().getIpAddress()).isEqualTo("198.51.100.8");
        assertThat(captor.getValue().getRequestMethod()).isEqualTo("PUT");
        assertThat(captor.getValue().getRequestPath()).isEqualTo("/api/inventory/1");
    }

    private AuditLogServiceImpl service(MockHttpServletRequest request, boolean trustForwardedHeaders) {
        when(usuarioRepository.findByUsername("medico")).thenReturn(Optional.empty());
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ObjectProvider<HttpServletRequest> requestProvider = new StaticListableBeanFactory(
                java.util.Map.of("auditRequest", request)).getBeanProvider(HttpServletRequest.class);
        AuditLogServiceImpl service = new AuditLogServiceImpl(auditLogRepository, usuarioRepository, requestProvider);
        ReflectionTestUtils.setField(service, "trustForwardedHeaders", trustForwardedHeaders);
        return service;
    }

    private MockHttpServletRequest request(String remoteAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/inventory/1");
        request.setRemoteAddr(remoteAddress);
        request.addHeader("User-Agent", "Audit test");
        return request;
    }

    private void authenticate() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "medico", "", List.of(new SimpleGrantedAuthority("ROLE_MEDICO")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
