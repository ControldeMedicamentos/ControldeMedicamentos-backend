package com.medicamentos.controller;

import com.medicamentos.dto.response.ReporteSISMEDDTO;
import com.medicamentos.service.ReporteSISMEDService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteSISMEDService reporteSISMEDService;

    @GetMapping("/sismed/{periodo}")
    public List<ReporteSISMEDDTO> generarReporteMensual(@PathVariable String periodo) {
        return reporteSISMEDService.generarReporteMensual(periodo);
    }

    @PostMapping("/sismed/{periodo}/cerrar")
    @ResponseStatus(HttpStatus.OK)
    public java.util.Map<String, Object> cerrarMes(@PathVariable String periodo) {
        int count = reporteSISMEDService.cerrarMes(periodo);
        return java.util.Map.of(
            "periodo", periodo,
            "medicamentosProcesados", count,
            "mensaje", "Cierre exitoso. Se generaron " + count + " saldos iniciales para el siguiente período."
        );
    }

    @GetMapping("/sismed/export")
    public ResponseEntity<byte[]> exportarReporteMensual(@RequestParam("period") String periodo) {
        byte[] excel = reporteSISMEDService.generarExcel(periodo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-sismed-" + periodo + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
