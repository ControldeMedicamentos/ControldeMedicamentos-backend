package com.medicamentos.service.Impl;

import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.mapper.PacienteMapper;
import com.medicamentos.repository.PacienteRepository;
import com.medicamentos.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;

    @Override
    public List<PacienteDTO> findAll() {
        return pacienteRepository.findAll().stream().map(pacienteMapper::toDTO).toList();
    }

    @Override
    public PacienteDTO findById(Long id) {
        return pacienteRepository.findById(id)
                .map(pacienteMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));
    }

    @Override
    public PacienteDTO findByNroDocumento(String nroDocumento) {
        return pacienteRepository.findByNroDocumento(nroDocumento)
                .map(pacienteMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + nroDocumento));
    }

    @Override
    public PacienteDTO create(PacienteCreateDTO request) {
        if (pacienteRepository.existsByNroDocumento(request.nroDocumento())) {
            throw new DuplicateResourceException("Ya existe un paciente con documento: " + request.nroDocumento());
        }
        return pacienteMapper.toDTO(pacienteRepository.save(pacienteMapper.toEntity(request)));
    }

    @Override
    public PacienteDTO update(Long id, PacienteCreateDTO request) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));

        if (!paciente.getNroDocumento().equals(request.nroDocumento())
                && pacienteRepository.existsByNroDocumento(request.nroDocumento())) {
            throw new DuplicateResourceException("Ya existe un paciente con documento: " + request.nroDocumento());
        }

        paciente.setTipoDocumento(request.tipoDocumento());
        paciente.setNroDocumento(request.nroDocumento());
        paciente.setNombresApellidos(request.nombresApellidos());
        paciente.setEdad(request.edad());
        paciente.setSexo(request.sexo());
        paciente.setCarreraArea(request.carreraArea());
        paciente.setCicloAcademico(request.cicloAcademico());
        paciente.setTelefono(request.telefono());

        return pacienteMapper.toDTO(pacienteRepository.save(paciente));
    }
}
