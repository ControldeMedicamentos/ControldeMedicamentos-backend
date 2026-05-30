package com.medicamentos.service;

import com.medicamentos.dto.request.EmpleadoCreateDTO;
import com.medicamentos.dto.request.EmpleadoUpdateDTO;
import com.medicamentos.dto.response.EmpleadoDTO;

import java.util.List;

public interface EmpleadoService {

    List<EmpleadoDTO> findAll();

    EmpleadoDTO create(EmpleadoCreateDTO dto);

    EmpleadoDTO update(Long id, EmpleadoUpdateDTO dto);

    EmpleadoDTO toggleEstado(Long id);
}
