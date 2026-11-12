package com.medicamentos.service;

import com.medicamentos.dto.request.ConciliacionMensualRequestDTO;
import com.medicamentos.dto.response.ConciliacionMensualDTO;
import com.medicamentos.dto.response.ReporteSISMEDDTO;

import java.util.List;

public interface ReporteSISMEDService {

    List<ReporteSISMEDDTO> generarReporteMensual(String periodo);

    byte[] generarExcel(String periodo);

    int cerrarMes(String periodo);

    boolean isPeriodoCerrado(String periodo);

    ConciliacionMensualDTO obtenerConciliacion(String periodo);

    ConciliacionMensualDTO conciliar(String periodo, ConciliacionMensualRequestDTO request, String usuarioRegistro);
}
