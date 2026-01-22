package com.neb.util;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.neb.entity.DailyReport;

public class ReportGeneratorPdf {

    /**
     * DAILY REPORT SUMMARY PDF
     * Uses ONLY existing entity fields
     */
    public byte[] generateDailyReportForEmployees(
            List<DailyReport> reports,
            LocalDate date) throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4.rotate(), 30f, 30f, 30f, 30f);
        PdfWriter.getInstance(document, baos);
        document.open();

        // ================= FONTS =================
        Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font rowFont    = FontFactory.getFont(FontFactory.HELVETICA, 11);

        // ================= TITLE =================
        Paragraph title = new Paragraph("Daily Report Summary", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // ================= DATE (RIGHT) =================
        Paragraph datePara = new Paragraph("Report Date: " + date, rowFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        document.add(datePara);

        document.add(Chunk.NEWLINE);

        // ================= TABLE =================
        
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.2f, 3f, 4f, 4f, 12f});
        table.setHeaderRows(1);

        // ================= HEADER ROW =================
        addHeaderCell(table, "Sr No", headerFont);
        addHeaderCell(table, "Card No", headerFont);
        addHeaderCell(table, "Employee Name", headerFont);
        addHeaderCell(table, "Designation", headerFont);
        addHeaderCell(table, "Summary", headerFont);

        // ================= DATA ROWS =================
        int srNo = 1;
        for (DailyReport report : reports) {

            // Sr No
            table.addCell(centerCell(String.valueOf(srNo++), rowFont));

            // Card Number
            table.addCell(normalCell(
                    report.getEmployee() != null
                            ? safe(report.getEmployee().getCardNumber())
                            : "", rowFont));

            // Employee Name
            String fullName = "";
            if (report.getEmployee() != null) {
                fullName = safe(report.getEmployee().getFirstName()) + " "
                         + safe(report.getEmployee().getLastName());
            }
            table.addCell(normalCell(fullName.trim(), rowFont));

            // Designation (EXISTS in entity)
            table.addCell(normalCell(
                    report.getEmployee() != null
                            ? safe(report.getEmployee().getDesignation())
                            : "", rowFont));

            // Summary
            PdfPCell summaryCell =
                    new PdfPCell(new Phrase(safe(report.getSummary()), rowFont));
            summaryCell.setPadding(6f);
            summaryCell.setNoWrap(false);
            summaryCell.setUseAscender(true);
            summaryCell.setUseDescender(true);
            table.addCell(summaryCell);
        }

        document.add(table);

        // ================= TOTAL COUNT =================
        Paragraph total = new Paragraph(
                "Total reports: " + reports.size(), headerFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10f);
        document.add(total);

        document.close();
        return baos.toByteArray();
    }

    // ================= HELPER METHODS =================

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.YELLOW);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);
        table.addCell(cell);
    }

    private PdfPCell normalCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6f);
        return cell;
    }

    private PdfPCell centerCell(String text, Font font) {
        PdfPCell cell = normalCell(text, font);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
