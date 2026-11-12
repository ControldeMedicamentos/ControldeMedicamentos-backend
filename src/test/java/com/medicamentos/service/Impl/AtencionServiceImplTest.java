package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.Paciente;
import com.medicamentos.dto.request.AtencionUpdateDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.mapper.AtencionMapper;
import com.medicamentos.repository.AtencionRepository;
import com.medicamentos.repository.ConsumoMedicamentoRepository;
import com.medicamentos.repository.PacienteRepository;
import com.medicamentos.service.InventarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtencionServiceImplTest {

    @Mock private AtencionRepository atencionRepository;
    @Mock private ConsumoMedicamentoRepository consumoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private InventarioService inventarioService;

    @Test
    void editaSoloDatosClinicosSinTocarInventarioNiIdentidad() {
        Atencion atencion = atencion();
        when(atencionRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(atencion));
        when(atencionRepository.saveAndFlush(atencion)).thenReturn(atencion);
        when(consumoRepository.findByAtencionId(7L)).thenReturn(List.of());

        var result = service().updateClinical(7L, request(atencion.getUpdatedAt()));

        assertThat(result.motivo()).isEqualTo("Control actualizado");
        assertThat(result.pacienteId()).isEqualTo(3L);
        assertThat(result.fechaEvaluacion()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(result.usuarioRegistro()).isEqualTo("autor-original");
        verify(atencionRepository).saveAndFlush(atencion);
        verifyNoInteractions(inventarioService, pacienteRepository);
    }

    @Test
    void rechazaEdicionBasadaEnVersionDesactualizada() {
        Atencion atencion = atencion();
        when(atencionRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(atencion));

        assertThatThrownBy(() -> service().updateClinical(7L, request(atencion.getUpdatedAt().minusMinutes(1))))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Recargue la página");
        verify(atencionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(consumoRepository, inventarioService);
    }

    private AtencionServiceImpl service() {
        return new AtencionServiceImpl(atencionRepository, consumoRepository, pacienteRepository,
                inventarioService, new AtencionMapper());
    }

    private Atencion atencion() {
        Paciente paciente = new Paciente();
        paciente.setId(3L);
        paciente.setNroDocumento("12345678");
        paciente.setNombresApellidos("Paciente prueba");
        Atencion atencion = new Atencion();
        atencion.setId(7L);
        atencion.setPaciente(paciente);
        atencion.setFechaEvaluacion(LocalDate.of(2026, 9, 1));
        atencion.setMotivo("Control inicial");
        atencion.setUsuarioRegistro("autor-original");
        atencion.setUpdatedAt(LocalDateTime.of(2026, 9, 22, 10, 0));
        return atencion;
    }

    private AtencionUpdateDTO request(LocalDateTime updatedAt) {
        return new AtencionUpdateDTO(updatedAt, " Control actualizado ", null, null, null,
                null, null, null, null, null, null, null, null, null, null, null,
                null, null, null);
    }
}
