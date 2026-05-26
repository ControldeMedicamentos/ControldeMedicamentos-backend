package com.medicamentos.dto.response;

public record ReporteSISMEDDTO(
        String periodo,
        String codigoSismed,
        String descripcionSismed,
        Integer saldoInicial,
        Integer ingresos,
        Integer reingresos,
        Integer consumos,
        Integer devoluciones,
        Integer vencidos,
        Integer merma,
        Integer distribucion,
        Integer transferencia,
        Integer stockFinal
) {
}
