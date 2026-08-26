package com.llacsaa.timesheet.report;

import com.llacsaa.timesheet.news.NewsView;
import com.llacsaa.timesheet.report.dto.MilestoneRow;
import com.llacsaa.timesheet.report.dto.PhaseProgressRow;
import com.llacsaa.timesheet.report.dto.ProjectProgressReport;
import com.llacsaa.timesheet.risk.RiskView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Exporta {@link ProjectProgressReport} a un .xlsx equivalente a la hoja
 * "Detalle" de AD-RE-04_TIME_SHEET_2026.xlsx (Fase 5, "exportación de
 * reportes" del roadmap) — mismas secciones que la vista Angular
 * (ProgressReportComponent), en el mismo orden: datos generales/situación
 * general, días del proyecto por fase (con fila TOTAL en negrita), hitos,
 * riesgos, novedades. La sección "Cambios aprobados" está deshabilitada en
 * pantalla ({@code changesSectionEnabled = false} en
 * progress-report.component.ts) y se excluye también aquí por consistencia.
 */
public final class ProjectProgressExcelExport {

    private ProjectProgressExcelExport() {
    }

    public static byte[] build(ProjectProgressReport report) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Avance de proyecto");
            CellStyle boldStyle = boldStyle(workbook);
            CellStyle titleStyle = titleStyle(workbook);

            int row = 0;
            row = writeTitle(sheet, row, titleStyle, "Reporte de avance de proyecto");
            row++;
            row = writeKeyValue(sheet, row, boldStyle, "Proyecto", report.getProjectName());
            row = writeKeyValue(sheet, row, boldStyle, "Cliente", report.getCustomerName());
            row = writeKeyValue(sheet, row, boldStyle, "Estado", report.getStatusName());
            row = writeKeyValue(sheet, row, boldStyle, "Líder", report.getLeaderName());
            row = writeKeyValue(sheet, row, boldStyle, "Fecha Inicio Proyecto", str(report.getStartDate()));
            row = writeKeyValue(sheet, row, boldStyle, "Fecha Informe Proyecto", str(report.getReportDate()));
            row = writeKeyValue(sheet, row, boldStyle, "Fecha Inicio Planificado", str(report.getPlannedStartDate()));
            row = writeKeyValue(sheet, row, boldStyle, "Fecha Fin Planificado", str(report.getPlannedEndDate()));
            row = writeKeyValue(sheet, row, boldStyle, "Fecha Inicio Real", str(report.getRealStartDate()));
            row = writeKeyValue(sheet, row, boldStyle, "Fecha Fin Real", str(report.getRealEndDate()));
            row++;

            row = writeTitle(sheet, row, titleStyle, "Días del proyecto por fase");
            row = writePhaseTable(sheet, row, boldStyle, report.getPhases());
            row++;

            row = writeTitle(sheet, row, titleStyle, "Hitos por Fase y Consultor");
            row = writeMilestoneTable(sheet, row, boldStyle, report.getMilestones());
            row++;

            row = writeTitle(sheet, row, titleStyle, "Manejo de riesgos");
            row = writeRiskTable(sheet, row, boldStyle, report.getRisks());
            row++;

            row = writeTitle(sheet, row, titleStyle, "Novedades presentadas");
            writeNewsTable(sheet, row, boldStyle, report.getNews());

            for (int c = 0; c < 15; c++) {
                sheet.autoSizeColumn(c);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static int writeTitle(XSSFSheet sheet, int rowIndex, CellStyle style, String title) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        return rowIndex + 1;
    }

    private static int writeKeyValue(XSSFSheet sheet, int rowIndex, CellStyle boldStyle, String key, String value) {
        Row row = sheet.createRow(rowIndex);
        Cell keyCell = row.createCell(0);
        keyCell.setCellValue(key);
        keyCell.setCellStyle(boldStyle);
        row.createCell(1).setCellValue(value != null ? value : "");
        return rowIndex + 1;
    }

    private static int writePhaseTable(XSSFSheet sheet, int rowIndex, CellStyle boldStyle, List<PhaseProgressRow> phases) {
        String[] headers = {
                "Fase", "Contratados", "Adendum", "Total", "Avance esperado (d)", "Avance real (d)",
                "Por invertir", "% Var. avance", "Días T.S.", "Saldo T.S.", "% Var. asig. planif.",
                "% Avance actual", "% Avance real", "% Efectividad"
        };
        int r = writeHeaderRow(sheet, rowIndex, boldStyle, headers);
        for (PhaseProgressRow p : phases) {
            Row row = sheet.createRow(r++);
            int c = 0;
            setStringCell(row, c++, p.getDescription(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getContractedDays(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getAddendumDays(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getTotalProjectDays(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getExpectedAdvanceDays(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getRealAdvanceDays(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getDaysToInvest(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getAdvanceVariationPerc(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getDaysConsumedTs(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getBalanceDaysTs(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getPlannedAssignmentVariationPerc(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getCurrentAdvancePerc(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c++, p.getRealAdvancePerc(), p.isTotal() ? boldStyle : null);
            setNumericCell(row, c, p.getEffectivenessPerc(), p.isTotal() ? boldStyle : null);
        }
        return r;
    }

    private static int writeMilestoneTable(XSSFSheet sheet, int rowIndex, CellStyle boldStyle, List<MilestoneRow> milestones) {
        String[] headers = {
                "Fase", "Hito", "Responsable", "Días base", "Adendum", "Total",
                "Fecha Inicio Base", "Fecha Fin Base", "% Var. asig. planif.", "% Avance real",
                "% Efectividad", "Días T.S."
        };
        int r = writeHeaderRow(sheet, rowIndex, boldStyle, headers);
        for (MilestoneRow m : milestones) {
            Row row = sheet.createRow(r++);
            int c = 0;
            setStringCell(row, c++, m.getParentDescription(), null);
            setStringCell(row, c++, m.getDescription(), null);
            setStringCell(row, c++, m.getMemberName(), null);
            setNumericCell(row, c++, m.getBaseDays(), null);
            setNumericCell(row, c++, m.getAddendumDays(), null);
            setNumericCell(row, c++, m.getTotalDays(), null);
            setStringCell(row, c++, str(m.getBaseStartDate()), null);
            setStringCell(row, c++, str(m.getBaseEndDate()), null);
            setNumericCell(row, c++, m.getPlannedAssignmentVariationPerc(), null);
            setNumericCell(row, c++, m.getRealAdvancePerc(), null);
            setNumericCell(row, c++, m.getEffectivenessPerc(), null);
            setNumericCell(row, c, m.getDaysConsumedTs(), null);
        }
        return r;
    }

    private static int writeRiskTable(XSSFSheet sheet, int rowIndex, CellStyle boldStyle, List<RiskView> risks) {
        String[] headers = {"Fecha", "Tipo impacto", "Descripción", "Responsable", "Compañía", "Solución", "Prob. %", "Estado"};
        int r = writeHeaderRow(sheet, rowIndex, boldStyle, headers);
        for (RiskView risk : risks) {
            Row row = sheet.createRow(r++);
            int c = 0;
            setStringCell(row, c++, str(risk.getRiskdate()), null);
            setStringCell(row, c++, risk.getRisktypeimpactName(), null);
            setStringCell(row, c++, risk.getRiskdescimpact(), null);
            setStringCell(row, c++, risk.getPersonincharge(), null);
            setStringCell(row, c++, risk.getCompany(), null);
            setStringCell(row, c++, risk.getSolution(), null);
            setNumericCell(row, c++, risk.getProbabilityperc(), null);
            setStringCell(row, c, risk.getRiskstatusName(), null);
        }
        return r;
    }

    private static int writeNewsTable(XSSFSheet sheet, int rowIndex, CellStyle boldStyle, List<NewsView> news) {
        String[] headers = {"Fecha", "Tipo impacto", "Descripción", "Reporta", "Afectación", "Responsable", "Compañía", "Solución", "Estado"};
        int r = writeHeaderRow(sheet, rowIndex, boldStyle, headers);
        for (NewsView n : news) {
            Row row = sheet.createRow(r++);
            int c = 0;
            setStringCell(row, c++, str(n.getDatenewarrival()), null);
            setStringCell(row, c++, n.getNewstypeimpactName(), null);
            setStringCell(row, c++, n.getDescriptionnews(), null);
            setStringCell(row, c++, n.getPersonreporting(), null);
            setStringCell(row, c++, n.getAffectation(), null);
            setStringCell(row, c++, n.getPersonincharge(), null);
            setStringCell(row, c++, n.getCompany(), null);
            setStringCell(row, c++, n.getSolution(), null);
            setStringCell(row, c, n.getNewstatusName(), null);
        }
        return r;
    }

    private static int writeHeaderRow(XSSFSheet sheet, int rowIndex, CellStyle boldStyle, String[] headers) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(boldStyle);
        }
        return rowIndex + 1;
    }

    private static void setStringCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private static void setNumericCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value.doubleValue());
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private static String str(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    private static CellStyle boldStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static CellStyle titleStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }
}
