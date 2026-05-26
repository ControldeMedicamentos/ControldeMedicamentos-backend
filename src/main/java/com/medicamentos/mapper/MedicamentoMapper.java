package com.medicamentos.mapper;

import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import org.springframework.stereotype.Component;

@Component
public class MedicamentoMapper {

    public Medicamento toEntity(MedicamentoCreateDTO dto) {
        Medicamento medicamento = new Medicamento();
        medicamento.setCodigoSismed(dto.codigoSismed());
        medicamento.setCodigoSiga(dto.codigoSiga());
        medicamento.setDescripcionSismed(dto.descripcionSismed());
        medicamento.setPresentacionFrasco(dto.presentacionFrasco());
        medicamento.setDescripcionCorta(dto.descripcionCorta());
        medicamento.setConversion(dto.conversion() == null ? 1 : dto.conversion());
        medicamento.setActivo(dto.activo() == null || dto.activo());
        return medicamento;
    }

    public MedicamentoDTO toDTO(Medicamento medicamento) {
        return new MedicamentoDTO(
                medicamento.getId(),
                medicamento.getCodigoSismed(),
                medicamento.getCodigoSiga(),
                medicamento.getDescripcionSismed(),
                medicamento.getPresentacionFrasco(),
                medicamento.getDescripcionCorta(),
                medicamento.getConversion(),
                medicamento.getActivo(),
                medicamento.getCreatedAt(),
                medicamento.getUpdatedAt()
        );
    }
}
