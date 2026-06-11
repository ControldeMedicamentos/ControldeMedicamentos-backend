package com.medicamentos.service;

import com.medicamentos.dto.request.AtencionCreateDTO;
import com.medicamentos.dto.response.AtencionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AtencionService {

    List<AtencionDTO> findAll();

    List<AtencionDTO> findByPaciente(Long pacienteId);

    List<AtencionDTO> findByFecha(LocalDate desde, LocalDate hasta);

    Page<AtencionDTO> findPageByFecha(LocalDate desde, LocalDate hasta, String search, Pageable pageable);

    AtencionDTO findById(Long id);

    AtencionDTO create(AtencionCreateDTO request);
}
