package com.medicamentos.dto.response;

import java.math.BigDecimal;

public record ReporteSISMEDDTO(
        String periodo,
        String codigoSismed,
        String descripcionSismed,
        BigDecimal precioUnitario,
        Integer saldoInicial,
        Integer ingresos,
        Integer reingresos,
        Integer venta,
        Integer sis,
        Integer intersanidad,
        Integer factoresPerdida,
        Integer defuncionNacimiento,
        Integer exonerado,
        Integer soat,
        Integer creditoHospitalario,
        Integer otroConvenio,
        Integer consumos,
        Integer devoluciones,
        Integer vencidos,
        Integer merma,
        Integer distribucion,
        Integer transferencia,
        Integer stockFinal
) {
}
