package com.medicamentos.service;

import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteDTO;

import java.util.List;

public interface PacienteService {

    List<PacienteDTO> findAll();

    PacienteDTO findById(Long id);

    PacienteDTO findByDni(String dni);

    PacienteDTO create(PacienteCreateDTO request);
}
