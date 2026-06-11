package com.medicamentos.service;

import com.medicamentos.dto.request.EmpleadoCreateDTO;
import com.medicamentos.dto.request.EmpleadoUpdateDTO;
import com.medicamentos.dto.response.EmpleadoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmpleadoService {

    List<EmpleadoDTO> findAll();

    Page<EmpleadoDTO> findPage(String search, String estado, Pageable pageable);

    EmpleadoDTO create(EmpleadoCreateDTO dto);

    EmpleadoDTO update(Long id, EmpleadoUpdateDTO dto);

    EmpleadoDTO toggleEstado(Long id);
}
