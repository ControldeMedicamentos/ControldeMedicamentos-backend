package com.medicamentos.dto.response;

import java.util.List;

public record DashboardDTO(
        long totalPacientesActivos,
        long atencionesMes,
        long medicamentosConBajoStock,
        long inventariosAlertaVencimiento,
        long totalMedicamentosActivos,
        List<AtencionResumenDTO> atencioneRecientes,
        List<InventarioDTO> stockAlertas,
        List<AtencionesPorDiaDTO> atencionesPorDia,
        List<TopConsumoDTO> topConsumos,
        List<AuditLogDTO> actividadReciente
) {
}
