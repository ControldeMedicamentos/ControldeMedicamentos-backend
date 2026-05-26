package com.medicamentos.mapper;

import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.ConsumoMedicamento;
import com.medicamentos.dto.response.AtencionDTO;
import com.medicamentos.dto.response.ConsumoMedicamentoResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AtencionMapper {

    public AtencionDTO toDTO(Atencion atencion, List<ConsumoMedicamento> consumos) {
        return new AtencionDTO(
                atencion.getId(),
                atencion.getPaciente().getId(),
                atencion.getPaciente().getDni(),
                atencion.getPaciente().getNombresApellidos(),
                atencion.getFechaEvaluacion(),
                atencion.getMotivo(),
                atencion.getAntecedentes(),
                atencion.getInmunizaciones(),
                atencion.getSignosVitales(),
                atencion.getExamenFisico(),
                atencion.getLaboratorio(),
                atencion.getDiagnostico1(),
                atencion.getCie101(),
                atencion.getTipoDiagnostico1(),
                atencion.getDiagnostico2(),
                atencion.getCie102(),
                atencion.getTipoDiagnostico2(),
                atencion.getDiagnostico3(),
                atencion.getCie103(),
                atencion.getTipoDiagnostico3(),
                atencion.getConclusion(),
                atencion.getDerivacion(),
                atencion.getObservaciones(),
                atencion.getUsuarioRegistro(),
                consumos.stream().map(this::toConsumoDTO).toList(),
                atencion.getCreatedAt(),
                atencion.getUpdatedAt()
        );
    }

    public ConsumoMedicamentoResponseDTO toConsumoDTO(ConsumoMedicamento consumo) {
        Long movimientoId = consumo.getMovimientoInventario() == null ? null : consumo.getMovimientoInventario().getId();
        return new ConsumoMedicamentoResponseDTO(
                consumo.getId(),
                consumo.getMedicamento().getId(),
                consumo.getMedicamento().getCodigoSismed(),
                consumo.getMedicamento().getDescripcionSismed(),
                consumo.getCantidadConsumida(),
                consumo.getTipoConsumo(),
                movimientoId,
                consumo.getCreatedAt()
        );
    }
}
