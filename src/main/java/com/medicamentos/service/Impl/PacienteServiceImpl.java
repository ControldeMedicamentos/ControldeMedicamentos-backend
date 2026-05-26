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
    public PacienteDTO findByDni(String dni) {
        return pacienteRepository.findByDni(dni)
                .map(pacienteMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + dni));
    }

    @Override
    public PacienteDTO create(PacienteCreateDTO request) {
        if (pacienteRepository.existsByDni(request.dni())) {
            throw new DuplicateResourceException("Ya existe un paciente con DNI: " + request.dni());
        }
        return pacienteMapper.toDTO(pacienteRepository.save(pacienteMapper.toEntity(request)));
    }
}
