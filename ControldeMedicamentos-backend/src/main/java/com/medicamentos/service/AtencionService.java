package com.medicamentos.service;

import com.medicamentos.dto.request.AtencionCreateDTO;
import com.medicamentos.dto.response.AtencionDTO;

import java.time.LocalDate;
import java.util.List;

public interface AtencionService {

    List<AtencionDTO> findAll();

    List<AtencionDTO> findByPaciente(Long pacienteId);

    List<AtencionDTO> findByFecha(LocalDate desde, LocalDate hasta);

    AtencionDTO findById(Long id);

    AtencionDTO create(AtencionCreateDTO request);
}
