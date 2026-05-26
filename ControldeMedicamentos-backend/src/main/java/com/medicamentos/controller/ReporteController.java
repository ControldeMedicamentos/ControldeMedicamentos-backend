package com.medicamentos.controller;

import com.medicamentos.dto.response.ReporteSISMEDDTO;
import com.medicamentos.service.ReporteSISMEDService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
