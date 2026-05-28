package com.medicamentos.service.Impl;

import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.mapper.MedicamentoMapper;
import com.medicamentos.repository.ConsumoMedicamentoRepository;
import com.medicamentos.repository.InventarioRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.service.MedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;
    private final InventarioRepository inventarioRepository;
    private final ConsumoMedicamentoRepository consumoRepository;

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
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamentoMapper.toEntity(request)));
    }

    @Override
    public MedicamentoDTO update(Long id, MedicamentoCreateDTO request) {
        var medicamento = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + id));

        medicamento.setNombre(request.nombre().trim());
        medicamento.setRegistroSanitario(request.registroSanitario());
        medicamento.setTipoProducto(request.tipoProducto());
        medicamento.setPresentacion(request.presentacion());
        medicamento.setFabricante(request.fabricante());
        medicamento.setPaisFabricacion(request.paisFabricacion());
        medicamento.setPrecioUnitario(request.precioUnitario());
        medicamento.setStockMinimo(request.stockMinimo() != null ? request.stockMinimo() : 0);
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

    @Override
    @Transactional
    public void delete(Long id) {
        if (!medicamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medicamento no encontrado: " + id);
        }
        if (consumoRepository.existsByMedicamentoId(id)) {
            throw new DuplicateResourceException(
                "No se puede eliminar: el medicamento tiene consumos registrados en atenciones."
            );
        }
        inventarioRepository.deleteAll(inventarioRepository.findByMedicamentoId(id));
        medicamentoRepository.deleteById(id);
    }
}
