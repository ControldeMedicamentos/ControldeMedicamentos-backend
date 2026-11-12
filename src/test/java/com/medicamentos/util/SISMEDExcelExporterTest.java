package com.medicamentos.util;

import com.medicamentos.dto.response.ReporteSISMEDDTO;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.CellType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SISMEDExcelExporterTest {

    @Test
    void exportaLaEstructuraOficialFormDet() throws Exception {
        ReporteSISMEDDTO reporte = new ReporteSISMEDDTO(
                "202608", "06377", "VACUNA ANTIAMARILICA", new BigDecimal("0.01"),
                10, 5, 1, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3,
                0, 0, 0, 0, 0, 13);

        byte[] contenido = new SISMEDExcelExporter().exportar(List.of(reporte), "202608");

        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(contenido))) {
            var sheet = workbook.getSheet("formDet");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getLastCellNum()).isEqualTo((short) 46);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("CODIGO_EJE");
            assertThat(sheet.getRow(0).getCell(45).getStringCellValue()).isEqualTo("MPTOREPO");
            assertThat(sheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("202608");
            assertThat(sheet.getRow(1).getCell(4).getStringCellValue()).isEqualTo("06377");
            assertThat(sheet.getRow(1).getCell(10).getNumericCellValue()).isEqualTo(3);
            assertThat(sheet.getRow(1).getCell(27).getNumericCellValue()).isEqualTo(13);
            assertThat(sheet.getRow(1).getCell(30).getNumericCellValue()).isEqualTo(3);
            assertThat(sheet.getRow(1).getCell(39).getCellType()).isEqualTo(CellType.NUMERIC);
        }
    }
}
