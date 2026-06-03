package com.medicamentos.service.Impl;

import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.mapper.PacienteMapper;
import com.medicamentos.repository.PacienteRepository;
import com.medicamentos.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<PacienteDTO> findPage(String search, String estado, Pageable pageable) {
        return pacienteRepository.findPage(normalize(search), normalizeEstado(estado), pageable)
                .map(pacienteMapper::toDTO);
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
        validateDocumento(request);
        if (pacienteRepository.existsByNroDocumento(request.nroDocumento())) {
            throw new DuplicateResourceException("Ya existe un paciente con documento: " + request.nroDocumento());
        }
        return pacienteMapper.toDTO(pacienteRepository.save(pacienteMapper.toEntity(request)));
    }

    @Override
    public PacienteDTO update(Long id, PacienteCreateDTO request) {
        validateDocumento(request);
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));

        if (!paciente.getNroDocumento().equals(request.nroDocumento())
                && pacienteRepository.existsByNroDocumento(request.nroDocumento())) {
            throw new DuplicateResourceException("Ya existe un paciente con documento: " + request.nroDocumento());
        }

        paciente.setTipoPaciente(request.tipoPaciente());
        paciente.setTipoDocumento(request.tipoDocumento());
        paciente.setNroDocumento(request.nroDocumento());
        paciente.setNombresApellidos(request.nombresApellidos());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setSexo(request.sexo());
        paciente.setCarreraArea(request.carreraArea());
        paciente.setCicloAcademico(request.cicloAcademico());
        paciente.setTelefono(request.telefono());

        return pacienteMapper.toDTO(pacienteRepository.save(paciente));
    }

    @Override
    public PacienteDTO toggleActivo(Long id) {
        var paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));
        paciente.setActivo(!paciente.isActivo());
        return pacienteMapper.toDTO(pacienteRepository.save(paciente));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEstado(String estado) {
        if ("inactivos".equals(estado) || "todos".equals(estado)) return estado;
        return "activos";
    }

    private void validateDocumento(PacienteCreateDTO request) {
        String documento = request.nroDocumento() == null ? "" : request.nroDocumento().trim();
        boolean valid = switch (request.tipoDocumento()) {
            case DNI -> documento.matches("^\\d{8}$");
            case CARNET_EXTRANJERIA -> documento.matches("^[A-Za-z0-9]{9,12}$");
            case PASAPORTE -> documento.matches("^[A-Za-z0-9]{6,15}$");
        };
        if (!valid) {
            throw new IllegalArgumentException("Documento inválido para el tipo seleccionado.");
        }
    }
}
