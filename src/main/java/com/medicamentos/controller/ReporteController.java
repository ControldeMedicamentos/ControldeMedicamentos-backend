package com.medicamentos.controller;

import com.medicamentos.dto.request.ConciliacionMensualRequestDTO;
import com.medicamentos.dto.response.ConciliacionMensualDTO;
import com.medicamentos.dto.response.ReporteSISMEDDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.ReporteSISMEDService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasAuthority('VISTA_REPORTES:LEER')")
    public java.util.Map<String, Boolean> isPeriodoCerrado(@PathVariable String periodo) {
        return java.util.Map.of("cerrado", reporteSISMEDService.isPeriodoCerrado(periodo));
    }

    @GetMapping("/sismed/{periodo}")
    @PreAuthorize("hasAuthority('VISTA_REPORTES:LEER')")
    public List<ReporteSISMEDDTO> generarReporteMensual(@PathVariable String periodo) {
        List<ReporteSISMEDDTO> result = reporteSISMEDService.generarReporteMensual(periodo);
        auditLogService.log("CONSULTAR_REPORTE", "Reportes",
                "Consulta del reporte SISMED", "PERIODO_SISMED", periodo);
        return result;
    }

    @GetMapping("/sismed/{periodo}/conciliacion")
    @PreAuthorize("hasAuthority('VISTA_REPORTES:LEER')")
    public ConciliacionMensualDTO obtenerConciliacion(@PathVariable String periodo) {
        return reporteSISMEDService.obtenerConciliacion(periodo);
    }

    @PostMapping("/sismed/{periodo}/conciliacion")
    @PreAuthorize("hasAuthority('VISTA_REPORTES:MODIFICAR')")
    public ConciliacionMensualDTO conciliar(
            @PathVariable String periodo,
            @Valid @RequestBody ConciliacionMensualRequestDTO request,
            Authentication authentication) {
        ConciliacionMensualDTO result = reporteSISMEDService.conciliar(
                periodo, request, authentication.getName());
        auditLogService.log("CONCILIAR_INVENTARIO", "Reportes",
                "Conciliación mensual: " + result.resultado(), "PERIODO_SISMED", periodo);
        return result;
    }

    @PostMapping("/sismed/{periodo}/cerrar")
    @PreAuthorize("hasAuthority('VISTA_REPORTES:MODIFICAR')")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> cerrarMes(@PathVariable String periodo) {
        int count = reporteSISMEDService.cerrarMes(periodo);
        auditLogService.log("CIERRE_MES", "Reportes",
                "Cierre de mes — " + count + " medicamentos procesados", "PERIODO_SISMED", periodo);
        return Map.of("periodo", periodo, "medicamentosProcesados", count,
                "mensaje", "Cierre exitoso. Se generaron " + count + " saldos iniciales para el siguiente período.");
    }

    @GetMapping("/sismed/export")
    @PreAuthorize("hasAuthority('VISTA_REPORTES:LEER')")
    public ResponseEntity<byte[]> exportarReporteMensual(@RequestParam("period") String periodo) {
        byte[] excel = reporteSISMEDService.generarExcel(periodo);
        auditLogService.log("EXPORTAR_REPORTE", "Reportes",
                "Reporte SISMED exportado a Excel", "PERIODO_SISMED", periodo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-sismed-" + periodo + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
