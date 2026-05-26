package com.medicamentos.service;

import com.medicamentos.dto.response.ReporteSISMEDDTO;

import java.util.List;

public interface ReporteSISMEDService {

    List<ReporteSISMEDDTO> generarReporteMensual(String periodo);

    byte[] generarExcel(String periodo);
}
