package com.medicamentos.util;

import com.medicamentos.dto.response.ReporteSISMEDDTO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SISMEDExcelExporter {

    private static final String[] HEADERS = {
            "CODIGO_EJE", "CODIGO_PRE", "TIPSUM", "ANNOMES", "CODIGO_MED",
            "SALDO", "PRECIO", "INGRE", "REINGRE", "VENTA", "SIS", "INTERSAN",
            "FAC_PERD", "DEFNAC", "EXO", "SOAT", "CREDHOSP", "OTR_CONV", "DEVOL",
            "VENCIDO", "MERMA", "DISTRI", "TRANSF", "VENTAINST", "DEV_VEN",
            "DEV_MERMA", "OTRAS_SAL", "STOCK_FIN", "STOCK_FIN1", "REQ", "TOTAL",
            "FEC_EXP", "DO_SALDO", "DO_INGRE", "DO_CON", "DO_OTR", "DO_TOT", "DO_STK",
            "DO_FECEXP", "FECHA", "USUARIO", "INDIPROC", "SIT", "INDISIGA", "DSTKCERO",
            "MPTOREPO"
    };

    @Value("${app.sismed.codigo-eje:}")
    private String codigoEje;

    @Value("${app.sismed.codigo-pre:}")
    private String codigoPre;

    public byte[] exportar(List<ReporteSISMEDDTO> reportes, String periodo) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("formDet");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            createTableHeader(sheet, headerStyle);
            fillRows(sheet, reportes, periodo, bodyStyle, dateStyle);
            resizeColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el reporte SISMED", ex);
        }
    }

    private void createTableHeader(Sheet sheet, CellStyle headerStyle) {
        Row row = sheet.createRow(0);
        for (int index = 0; index < HEADERS.length; index++) {
            createCell(row, index, HEADERS[index], headerStyle);
        }
    }

    private void fillRows(
            Sheet sheet,
            List<ReporteSISMEDDTO> reportes,
            String periodo,
            CellStyle bodyStyle,
            CellStyle dateStyle
    ) {
        int rowIndex = 1;
        LocalDate fechaReporte = YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyyMM")).atEndOfMonth();
        for (ReporteSISMEDDTO r : reportes) {
            Row row = sheet.createRow(rowIndex++);
            createCell(row, 0, valueOrEmpty(codigoEje), bodyStyle);
            createCell(row, 1, valueOrEmpty(codigoPre), bodyStyle);
            createCell(row, 2, "S", bodyStyle);
            createCell(row, 3, periodo, bodyStyle);
            createCell(row, 4, valueOrEmpty(r.codigoSismed()), bodyStyle);
            createNumericCell(row, 5, r.saldoInicial(), bodyStyle);
            createNumericCell(row, 6, r.precioUnitario() == null ? 0 : r.precioUnitario().doubleValue(), bodyStyle);
            createNumericCell(row, 7, r.ingresos(), bodyStyle);
            createNumericCell(row, 8, r.reingresos(), bodyStyle);
            createNumericCell(row, 9, r.venta(), bodyStyle);
            createNumericCell(row, 10, r.sis(), bodyStyle);
            createNumericCell(row, 11, r.intersanidad(), bodyStyle);
            createNumericCell(row, 12, r.factoresPerdida(), bodyStyle);
            createNumericCell(row, 13, r.defuncionNacimiento(), bodyStyle);
            createNumericCell(row, 14, r.exonerado(), bodyStyle);
            createNumericCell(row, 15, r.soat(), bodyStyle);
            createNumericCell(row, 16, r.creditoHospitalario(), bodyStyle);
            createNumericCell(row, 17, r.otroConvenio(), bodyStyle);
            createNumericCell(row, 18, r.devoluciones(), bodyStyle);
            createNumericCell(row, 19, r.vencidos(), bodyStyle);
            createNumericCell(row, 20, r.merma(), bodyStyle);
            createNumericCell(row, 21, r.distribucion(), bodyStyle);
            createNumericCell(row, 22, r.transferencia(), bodyStyle);
            fillZeros(row, 23, 26, bodyStyle);
            createNumericCell(row, 27, r.stockFinal(), bodyStyle);
            createNumericCell(row, 28, r.stockFinal(), bodyStyle);
            createNumericCell(row, 29, 0, bodyStyle);
            createNumericCell(row, 30, r.consumos(), bodyStyle);
            createCell(row, 31, "", bodyStyle);
            fillZeros(row, 32, 38, bodyStyle);
            createDateCell(row, 39, fechaReporte, dateStyle);
            createCell(row, 40, "SISTEMA", bodyStyle);
            createCell(row, 41, "", bodyStyle);
            createCell(row, 42, "1", bodyStyle);
            createCell(row, 43, "", bodyStyle);
            fillZeros(row, 44, 45, bodyStyle);
        }
    }

    private void fillZeros(Row row, int from, int to, CellStyle style) {
        for (int column = from; column <= to; column++) {
            createNumericCell(row, column, 0, style);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createBodyStyle(Workbook workbook) {
        return createBorderedStyle(workbook);
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private CellStyle createBorderedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int column, Integer value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value == null ? 0 : value);
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createDateCell(Row row, int column, LocalDate value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void resizeColumns(Sheet sheet) {
        for (int index = 0; index < HEADERS.length; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
