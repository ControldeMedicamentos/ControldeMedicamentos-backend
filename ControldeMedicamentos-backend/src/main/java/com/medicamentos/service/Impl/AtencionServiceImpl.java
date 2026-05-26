package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.ConsumoMedicamento;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.Paciente;
import com.medicamentos.dto.request.AtencionCreateDTO;
import com.medicamentos.dto.request.ConsumoMedicamentoDTO;
import com.medicamentos.dto.response.AtencionDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.mapper.AtencionMapper;
import com.medicamentos.repository.AtencionRepository;
import com.medicamentos.repository.ConsumoMedicamentoRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.PacienteRepository;
import com.medicamentos.service.AtencionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AtencionServiceImpl implements AtencionService {

    private final AtencionRepository atencionRepository;
    private final ConsumoMedicamentoRepository consumoMedicamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AtencionMapper atencionMapper;

    @Override
    public List<AtencionDTO> findAll() {
        return atencionRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public List<AtencionDTO> findByPaciente(Long pacienteId) {
        return atencionRepository.findByPacienteId(pacienteId).stream().map(this::toDTO).toList();
    }

    @Override
    public List<AtencionDTO> findByFecha(LocalDate desde, LocalDate hasta) {
        return atencionRepository.findByFechaEvaluacionBetween(desde, hasta).stream().map(this::toDTO).toList();
    }

    @Override
    public AtencionDTO findById(Long id) {
        Atencion atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atencion no encontrada: " + id));
        return toDTO(atencion);
    }

    @Override
    @Transactional
    public AtencionDTO create(AtencionCreateDTO request) {
        Paciente paciente = pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + request.pacienteId()));
        Atencion atencion = buildAtencion(request, paciente);
        Atencion savedAtencion = atencionRepository.save(atencion);
        List<ConsumoMedicamento> consumos = saveConsumos(request.consumos(), savedAtencion);
        return atencionMapper.toDTO(savedAtencion, consumos);
    }

    private AtencionDTO toDTO(Atencion atencion) {
        List<ConsumoMedicamento> consumos = consumoMedicamentoRepository.findByAtencionId(atencion.getId());
        return atencionMapper.toDTO(atencion, consumos);
    }

    private Atencion buildAtencion(AtencionCreateDTO request, Paciente paciente) {
        Atencion atencion = new Atencion();
        atencion.setPaciente(paciente);
        atencion.setFechaEvaluacion(request.fechaEvaluacion());
        atencion.setMotivo(request.motivo());
        atencion.setAntecedentes(request.antecedentes());
        atencion.setInmunizaciones(request.inmunizaciones());
        atencion.setSignosVitales(request.signosVitales());
        atencion.setExamenFisico(request.examenFisico());
        atencion.setLaboratorio(request.laboratorio());
        atencion.setDiagnostico1(request.diagnostico1());
        atencion.setCie101(request.cie101());
        atencion.setTipoDiagnostico1(request.tipoDiagnostico1());
        atencion.setDiagnostico2(request.diagnostico2());
        atencion.setCie102(request.cie102());
        atencion.setTipoDiagnostico2(request.tipoDiagnostico2());
        atencion.setDiagnostico3(request.diagnostico3());
        atencion.setCie103(request.cie103());
        atencion.setTipoDiagnostico3(request.tipoDiagnostico3());
        atencion.setConclusion(request.conclusion());
        atencion.setDerivacion(request.derivacion());
        atencion.setObservaciones(request.observaciones());
        atencion.setUsuarioRegistro(request.usuarioRegistro());
        return atencion;
    }

    private List<ConsumoMedicamento> saveConsumos(List<ConsumoMedicamentoDTO> requests, Atencion atencion) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<ConsumoMedicamento> consumos = requests.stream()
                .map(request -> buildConsumo(request, atencion))
                .toList();
        return consumoMedicamentoRepository.saveAll(consumos);
    }

    private ConsumoMedicamento buildConsumo(ConsumoMedicamentoDTO request, Atencion atencion) {
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + request.medicamentoId()));
        ConsumoMedicamento consumo = new ConsumoMedicamento();
        consumo.setAtencion(atencion);
        consumo.setMedicamento(medicamento);
        consumo.setCantidadConsumida(request.cantidadConsumida());
        consumo.setTipoConsumo(request.tipoConsumo());
        return consumo;
    }
}
