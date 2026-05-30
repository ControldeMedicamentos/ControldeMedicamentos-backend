package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.Inventario;
import com.medicamentos.dto.response.AtencionResumenDTO;
import com.medicamentos.dto.response.AtencionesPorDiaDTO;
import com.medicamentos.dto.response.AuditLogDTO;
import com.medicamentos.dto.response.DashboardDTO;
import com.medicamentos.dto.response.InventarioDTO;
import com.medicamentos.dto.response.TopConsumoDTO;
import com.medicamentos.mapper.InventarioMapper;
import com.medicamentos.repository.AtencionRepository;
import com.medicamentos.repository.AuditLogRepository;
import com.medicamentos.repository.ConsumoMedicamentoRepository;
import com.medicamentos.repository.InventarioRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.PacienteRepository;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PacienteRepository pacienteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AtencionRepository atencionRepository;
    private final InventarioRepository inventarioRepository;
    private final ConsumoMedicamentoRepository consumoRepository;
    private final InventarioMapper inventarioMapper;
    private final AuditLogService auditLogService;

    @Override
    public DashboardDTO getStats() {
        LocalDate hoy = LocalDate.now();
        LocalDate primerDiaMes = hoy.withDayOfMonth(1);
        LocalDate en30Dias = hoy.plusDays(30);
        LocalDate hace13Dias = hoy.minusDays(13);

        long totalPacientes = pacienteRepository.countByActivoTrue();
        long totalMedicamentos = medicamentoRepository.countByActivoTrue();
        long atencionesMes = atencionRepository.countByFechaEvaluacionBetween(primerDiaMes, hoy);

        List<Inventario> todos = inventarioRepository.findAll();

        long bajosStock = todos.stream()
                .filter(i -> i.getStockActual() <= i.getStockMinimo())
                .count();

        long alertasVencimiento = todos.stream()
                .filter(i -> i.getFechaVencimiento() != null && !i.getFechaVencimiento().isAfter(en30Dias))
                .count();

        List<InventarioDTO> stockAlertas = todos.stream()
                .filter(i -> i.getStockActual() <= i.getStockMinimo()
                        || (i.getFechaVencimiento() != null && !i.getFechaVencimiento().isAfter(en30Dias)))
                .map(inventarioMapper::toDTO)
                .limit(20)
                .toList();

        List<AtencionResumenDTO> recientes = atencionRepository
                .findTop5ByOrderByFechaEvaluacionDescIdDesc()
                .stream()
                .map(this::toResumen)
                .toList();

        Map<LocalDate, Long> porDiaMap = atencionRepository.countGroupedByDay(hace13Dias, hoy)
                .stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> (Long) row[1]
                ));

        List<AtencionesPorDiaDTO> atencionesPorDia = hace13Dias.datesUntil(hoy.plusDays(1))
                .map(d -> new AtencionesPorDiaDTO(d.toString(), porDiaMap.getOrDefault(d, 0L)))
                .toList();

        List<TopConsumoDTO> topConsumos = consumoRepository.findTopConsumosRaw(primerDiaMes, hoy)
                .stream()
                .limit(5)
                .map(row -> new TopConsumoDTO((String) row[0], ((Number) row[1]).longValue()))
                .toList();

        List<AuditLogDTO> actividadReciente = auditLogService.getRecent(8);

        return new DashboardDTO(
                totalPacientes,
                atencionesMes,
                bajosStock,
                alertasVencimiento,
                totalMedicamentos,
                recientes,
                stockAlertas,
                atencionesPorDia,
                topConsumos,
                actividadReciente
        );
    }

    private AtencionResumenDTO toResumen(Atencion atencion) {
        int consumos = consumoRepository.findByAtencionId(atencion.getId()).size();
        return new AtencionResumenDTO(
                atencion.getId(),
                atencion.getPaciente().getNombresApellidos(),
                atencion.getPaciente().getNroDocumento(),
                atencion.getFechaEvaluacion(),
                atencion.getMotivo(),
                consumos
        );
    }
}
