package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Inventario;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.dto.response.InventarioDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.mapper.InventarioMapper;
import com.medicamentos.repository.InventarioRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final InventarioMapper inventarioMapper;

    @Override
    public List<InventarioDTO> findAll() {
        return inventarioRepository.findAll().stream().map(inventarioMapper::toDTO).toList();
    }

    @Override
    public List<InventarioDTO> findByMedicamento(Long medicamentoId) {
        return inventarioRepository.findByMedicamentoId(medicamentoId).stream()
                .map(inventarioMapper::toDTO)
                .toList();
    }

    @Override
    public InventarioDTO create(InventarioCreateDTO request) {
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + request.medicamentoId()));
        Inventario inventario = new Inventario();
        inventario.setMedicamento(medicamento);
        inventario.setStockActual(request.stockActual());
        inventario.setStockMinimo(request.stockMinimo());
        inventario.setLote(request.lote());
        inventario.setFechaVencimiento(request.fechaVencimiento());
        return inventarioMapper.toDTO(inventarioRepository.save(inventario));
    }
}
