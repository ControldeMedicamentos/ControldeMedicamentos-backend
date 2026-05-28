package com.medicamentos.mapper;

import com.medicamentos.domain.model.Paciente;
import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteDTO;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {

    public Paciente toEntity(PacienteCreateDTO dto) {
        Paciente paciente = new Paciente();
        paciente.setTipoPaciente(dto.tipoPaciente());
        paciente.setTipoDocumento(dto.tipoDocumento());
        paciente.setNroDocumento(dto.nroDocumento());
        paciente.setNombresApellidos(dto.nombresApellidos());
        paciente.setFechaNacimiento(dto.fechaNacimiento());
        paciente.setSexo(dto.sexo());
        paciente.setCarreraArea(dto.carreraArea());
        paciente.setCicloAcademico(dto.cicloAcademico());
        paciente.setTelefono(dto.telefono());
        return paciente;
    }

    public PacienteDTO toDTO(Paciente paciente) {
        return new PacienteDTO(
                paciente.getId(),
                paciente.getTipoPaciente(),
                paciente.getTipoDocumento(),
                paciente.getNroDocumento(),
                paciente.getNombresApellidos(),
                paciente.getFechaNacimiento(),
                paciente.getSexo(),
                paciente.getCarreraArea(),
                paciente.getCicloAcademico(),
                paciente.getTelefono(),
                paciente.isActivo(),
                paciente.getCreatedAt(),
                paciente.getUpdatedAt()
        );
    }
}
