package com.medicamentos.controller;

import com.medicamentos.dto.response.ReporteSISMEDDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.ReporteSISMEDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteSISMEDService reporteSISMEDService;
    private final AuditLogService auditLogService;

    @GetMapping("/sismed/{periodo}/cerrado")
    public java.util.Map<String, Boolean> isPeriodoCerrado(@PathVariable String periodo) {
        return java.util.Map.of("cerrado", reporteSISMEDService.isPeriodoCerrado(periodo));
    }

    @GetMapping("/sismed/{periodo}")
    public List<ReporteSISMEDDTO> generarReporteMensual(@PathVariable String periodo) {
        auditLogService.log("CONSULTAR_REPORTE", "Reportes",
                "Consulta del reporte SISMED período: " + periodo);
        return reporteSISMEDService.generarReporteMensual(periodo);
    }

    @PostMapping("/sismed/{periodo}/cerrar")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> cerrarMes(@PathVariable String periodo) {
        int count = reporteSISMEDService.cerrarMes(periodo);
        auditLogService.log("CIERRE_MES", "Reportes",
                "Cierre de mes ejecutado — Período: " + periodo + " — " + count + " medicamentos procesados");
        return Map.of("periodo", periodo, "medicamentosProcesados", count,
                "mensaje", "Cierre exitoso. Se generaron " + count + " saldos iniciales para el siguiente período.");
    }

    @GetMapping("/sismed/export")
    public ResponseEntity<byte[]> exportarReporteMensual(@RequestParam("period") String periodo) {
        byte[] excel = reporteSISMEDService.generarExcel(periodo);
        auditLogService.log("EXPORTAR_REPORTE", "Reportes",
                "Reporte SISMED exportado a Excel — Período: " + periodo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-sismed-" + periodo + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
