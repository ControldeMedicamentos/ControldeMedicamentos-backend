package com.medicamentos.service.Impl;

import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.mapper.MedicamentoMapper;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.service.MedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    @Override
    public List<MedicamentoDTO> findAll() {
        return medicamentoRepository.findAll().stream().map(medicamentoMapper::toDTO).toList();
    }

    @Override
    public MedicamentoDTO findById(Long id) {
        return medicamentoRepository.findById(id)
                .map(medicamentoMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + id));
    }

    @Override
    public MedicamentoDTO findByCodigoSismed(String codigoSismed) {
        return medicamentoRepository.findByCodigoSismed(codigoSismed)
                .map(medicamentoMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + codigoSismed));
    }

    @Override
    public MedicamentoDTO create(MedicamentoCreateDTO request) {
        if (medicamentoRepository.existsByCodigoSismed(request.codigoSismed())) {
            throw new DuplicateResourceException("Ya existe un medicamento con codigo SISMED: " + request.codigoSismed());
        }
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamentoMapper.toEntity(request)));
    }

    @Override
    public MedicamentoDTO update(Long id, MedicamentoCreateDTO request) {
        var medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + id));

        if (!medicamento.getCodigoSismed().equals(request.codigoSismed())
                && medicamentoRepository.existsByCodigoSismed(request.codigoSismed())) {
            throw new DuplicateResourceException("Ya existe un medicamento con codigo SISMED: " + request.codigoSismed());
        }

        medicamento.setCodigoSismed(request.codigoSismed());
        medicamento.setCodigoSiga(request.codigoSiga());
        medicamento.setDescripcionSismed(request.descripcionSismed());
        medicamento.setPresentacionFrasco(request.presentacionFrasco());
        medicamento.setDescripcionCorta(request.descripcionCorta());
        medicamento.setConversion(request.conversion() == null ? 1 : request.conversion());
        medicamento.setActivo(request.activo() == null || request.activo());

        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamento));
    }

    @Override
    public MedicamentoDTO toggleActivo(Long id) {
        var medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + id));
        medicamento.setActivo(!medicamento.getActivo());
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamento));
    }
}
