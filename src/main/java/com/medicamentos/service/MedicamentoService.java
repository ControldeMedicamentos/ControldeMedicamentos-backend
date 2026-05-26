package com.medicamentos.service;

import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;

import java.util.List;

public interface MedicamentoService {

    List<MedicamentoDTO> findAll();

    MedicamentoDTO findById(Long id);

    MedicamentoDTO findByCodigoSismed(String codigoSismed);

    MedicamentoDTO create(MedicamentoCreateDTO request);
}
