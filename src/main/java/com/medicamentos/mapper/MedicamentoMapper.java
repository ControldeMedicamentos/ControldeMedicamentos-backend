package com.medicamentos.mapper;

import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import org.springframework.stereotype.Component;

@Component
public class MedicamentoMapper {

    public Medicamento toEntity(MedicamentoCreateDTO dto) {
        Medicamento m = new Medicamento();
        m.setNombre(dto.nombre().trim());
        m.setRegistroSanitario(dto.registroSanitario());
        m.setTipoProducto(dto.tipoProducto());
        m.setPresentacion(dto.presentacion());
        m.setFabricante(dto.fabricante());
        m.setPaisFabricacion(dto.paisFabricacion());
        m.setPrecioUnitario(dto.precioUnitario());
        m.setStockMinimo(dto.stockMinimo() != null ? dto.stockMinimo() : 0);
        m.setActivo(dto.activo() == null || dto.activo());
        return m;
    }

    public MedicamentoDTO toDTO(Medicamento m) {
        return new MedicamentoDTO(
                m.getId(),
                m.getNombre(),
                m.getRegistroSanitario(),
                m.getTipoProducto(),
                m.getPresentacion(),
                m.getFabricante(),
                m.getPaisFabricacion(),
                m.getPrecioUnitario(),
                m.getStockMinimo(),
                m.getCodigoSismed(),
                m.getDescripcionSismed(),
                m.getActivo(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
