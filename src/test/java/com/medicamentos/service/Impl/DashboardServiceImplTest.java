package com.medicamentos.service.Impl;

import com.medicamentos.dto.response.DashboardDTO;
import com.medicamentos.mapper.InventarioMapper;
import com.medicamentos.repository.AtencionRepository;
import com.medicamentos.repository.ConsumoMedicamentoRepository;
import com.medicamentos.repository.InventarioRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.PacienteRepository;
import com.medicamentos.service.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock private PacienteRepository pacienteRepository;
    @Mock private MedicamentoRepository medicamentoRepository;
    @Mock private AtencionRepository atencionRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private ConsumoMedicamentoRepository consumoRepository;
    @Mock private AuditLogService auditLogService;

    private DashboardServiceImpl service;

    @BeforeEach
    void createService() {
        service = new DashboardServiceImpl(pacienteRepository, medicamentoRepository,
                atencionRepository, inventarioRepository, consumoRepository,
                new InventarioMapper(), auditLogService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noConsultaNiExponeModulosSinPermisoDeLectura() {
        authenticate("VISTA_DASHBOARD:LEER");

        DashboardDTO result = service.getStats();

        assertThat(result.totalPacientesActivos()).isZero();
        assertThat(result.atencionesMes()).isZero();
        assertThat(result.stockAlertas()).isEmpty();
        assertThat(result.atencioneRecientes()).isEmpty();
        assertThat(result.topConsumos()).isEmpty();
        assertThat(result.actividadReciente()).isEmpty();
        verifyNoInteractions(pacienteRepository, medicamentoRepository, atencionRepository,
                inventarioRepository, consumoRepository, auditLogService);
    }

    @Test
    void noExponeIdentidadDelPacienteSiSoloPuedeLeerAtenciones() {
        authenticate("VISTA_DASHBOARD:LEER", "VISTA_ATENCIONES:LEER");
        when(atencionRepository.countGroupedByDay(any(), any())).thenReturn(List.of());

        DashboardDTO result = service.getStats();

        assertThat(result.atencionesPorDia()).hasSize(14);
        assertThat(result.atencioneRecientes()).isEmpty();
        assertThat(result.topConsumos()).isEmpty();
        verifyNoInteractions(pacienteRepository, medicamentoRepository, inventarioRepository,
                consumoRepository, auditLogService);
    }

    private void authenticate(String... authorities) {
        var granted = List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("usuario", null, granted));
    }
}
