package com.medicamentos.dto.response;

public record ReporteSISMEDDTO(
        String periodo,
        String nombreMedicamento,
        Integer saldoInicial,
        Integer ingresos,
        Integer reingresos,
        Integer alumno,
        Integer docente,
        Integer administrativo,
        Integer invitado,
        Integer consumos,
        Integer devoluciones,
        Integer vencidos,
        Integer merma,
        Integer distribucion,
        Integer transferencia,
        Integer stockFinal
) {
}
