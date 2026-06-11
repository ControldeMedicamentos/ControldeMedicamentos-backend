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
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class SISMEDExcelExporter {

    private static final String[] HEADERS = {
            "MEDICAMENTO", "SALDO", "INGRE", "REINGRE",
            "ALUMNO", "DOCENTE", "ADMIN", "INVITADO",
            "CONSUMOS", "DEVOLUCIONES", "VENCIDOS", "MERMA",
            "DISTRIBUCION", "TRANSFERENCIA", "STOCK_FINAL", "VALIDACION"
    };

    public byte[] exportar(List<ReporteSISMEDDTO> reportes, String periodo) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("formDet");
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle bodyStyle = createBodyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            createHeader(sheet, titleStyle, periodo);
            createTableHeader(sheet, headerStyle);
            int totalRowIndex = fillRows(sheet, reportes, bodyStyle);
            createTotalsRow(sheet, totalRowIndex, totalStyle);
            resizeColumns(sheet);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar el reporte SISMED", ex);
        }
    }

    private void createHeader(Sheet sheet, CellStyle titleStyle, String periodo) {
        Row title = sheet.createRow(0);
        createCell(title, 0, "REPORTE SISMED / SIGA", titleStyle);
        Row establecimiento = sheet.createRow(1);
        createCell(establecimiento, 0, "Establecimiento", titleStyle);
        createCell(establecimiento, 1, "TOPICO UNIVERSITARIO", titleStyle);
        Row periodoRow = sheet.createRow(2);
        createCell(periodoRow, 0, "Periodo", titleStyle);
        createCell(periodoRow, 1, periodo, titleStyle);
        Row tipoRow = sheet.createRow(3);
        createCell(tipoRow, 0, "Tipo", titleStyle);
        createCell(tipoRow, 1, "SISMED", titleStyle);
    }

    private void createTableHeader(Sheet sheet, CellStyle headerStyle) {
        Row row = sheet.createRow(5);
        for (int index = 0; index < HEADERS.length; index++) {
            createCell(row, index, HEADERS[index], headerStyle);
        }
    }

    private int fillRows(Sheet sheet, List<ReporteSISMEDDTO> reportes, CellStyle bodyStyle) {
        int rowIndex = 6;
        for (ReporteSISMEDDTO r : reportes) {
            Row row = sheet.createRow(rowIndex++);
            createCell(row, 0, r.nombreMedicamento(), bodyStyle);
            createNumericCell(row, 1, r.saldoInicial(), bodyStyle);
            createNumericCell(row, 2, r.ingresos(), bodyStyle);
            createNumericCell(row, 3, r.reingresos(), bodyStyle);
            createNumericCell(row, 4, r.alumno(), bodyStyle);
            createNumericCell(row, 5, r.docente(), bodyStyle);
            createNumericCell(row, 6, r.administrativo(), bodyStyle);
            createNumericCell(row, 7, r.invitado(), bodyStyle);
            createNumericCell(row, 8, r.consumos(), bodyStyle);
            createNumericCell(row, 9, r.devoluciones(), bodyStyle);
            createNumericCell(row, 10, r.vencidos(), bodyStyle);
            createNumericCell(row, 11, r.merma(), bodyStyle);
            createNumericCell(row, 12, r.distribucion(), bodyStyle);
            createNumericCell(row, 13, r.transferencia(), bodyStyle);
            createNumericCell(row, 14, r.stockFinal(), bodyStyle);
            createCell(row, 15, "OK", bodyStyle);
        }
        return rowIndex;
    }

    private void createTotalsRow(Sheet sheet, int rowIndex, CellStyle totalStyle) {
        Row row = sheet.createRow(rowIndex);
        createCell(row, 0, "TOTALES", totalStyle);
        for (int index = 1; index <= 14; index++) {
            String column = columnName(index);
            createFormulaCell(row, index, "SUM(" + column + "7:" + column + rowIndex + ")", totalStyle);
        }
        int tr = rowIndex + 1;
        createFormulaCell(row, 15,
                "IF(O" + tr + "=B" + tr + "+C" + tr + "+D" + tr
                + "-I" + tr + "-J" + tr + "-K" + tr + "-L" + tr
                + "-M" + tr + "-N" + tr + ",\"OK\",\"REVISAR\")", totalStyle);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
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

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = createBorderedStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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

    private void createFormulaCell(Row row, int column, String formula, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellFormula(formula);
        cell.setCellStyle(style);
    }

    private void resizeColumns(Sheet sheet) {
        for (int index = 0; index < HEADERS.length; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    private String columnName(int zeroBasedIndex) {
        int index = zeroBasedIndex + 1;
        StringBuilder column = new StringBuilder();
        while (index > 0) {
            int remainder = (index - 1) % 26;
            column.insert(0, (char) ('A' + remainder));
            index = (index - 1) / 26;
        }
        return column.toString();
    }
}
