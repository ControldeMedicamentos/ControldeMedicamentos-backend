package com.medicamentos.service;

import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MedicamentoService {

    List<MedicamentoDTO> findAll();

    Page<MedicamentoDTO> findPage(String search, String estado, Pageable pageable);

    MedicamentoDTO findById(Long id);

    MedicamentoDTO findByCodigoSismed(String codigoSismed);

    MedicamentoDTO create(MedicamentoCreateDTO request);

    MedicamentoDTO update(Long id, MedicamentoCreateDTO request);

    MedicamentoDTO toggleActivo(Long id);

    void delete(Long id);
}
