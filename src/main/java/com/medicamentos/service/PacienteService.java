package com.medicamentos.service;

import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PacienteService {

    List<PacienteDTO> findAll();

    Page<PacienteDTO> findPage(String search, String estado, Pageable pageable);

    PacienteDTO findById(Long id);

    PacienteDTO findByNroDocumento(String nroDocumento);

    PacienteDTO create(PacienteCreateDTO request);

    PacienteDTO update(Long id, PacienteCreateDTO request);

    PacienteDTO toggleActivo(Long id);
}
