package com.medicamentos.mapper;

import com.medicamentos.domain.model.Paciente;
import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteDTO;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {

    public Paciente toEntity(PacienteCreateDTO dto) {
        Paciente paciente = new Paciente();
        paciente.setTipoDocumento(dto.tipoDocumento());
        paciente.setNroDocumento(dto.nroDocumento());
        paciente.setNombresApellidos(dto.nombresApellidos());
        paciente.setEdad(dto.edad());
        paciente.setSexo(dto.sexo());
        paciente.setCarreraArea(dto.carreraArea());
        paciente.setCicloAcademico(dto.cicloAcademico());
        paciente.setTelefono(dto.telefono());
        return paciente;
    }

    public PacienteDTO toDTO(Paciente paciente) {
        return new PacienteDTO(
                paciente.getId(),
                paciente.getTipoDocumento(),
                paciente.getNroDocumento(),
                paciente.getNombresApellidos(),
                paciente.getEdad(),
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
