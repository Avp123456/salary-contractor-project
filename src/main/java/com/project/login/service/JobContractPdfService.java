package com.project.login.service;

import com.project.login.entity.gen_bill;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class JobContractPdfService {

    public ByteArrayInputStream export(List<gen_bill> list) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDocument = new PdfDocument(writer);

            // Landscape A4
            Document document = new Document(pdfDocument, PageSize.A4.rotate());
            document.setMargins(20, 20, 20, 20);

            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ===== Title =====
            Paragraph title = new Paragraph("JOB CONTRACT REPORT")
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER);

            document.add(title);
            document.add(new Paragraph(" "));

            // 16 Columns
            float[] columnWidths = {
                    70f, 80f, 100f, 100f, 100f,
                    70f, 60f, 70f, 70f,
                    120f, 60f, 120f,
                    80f, 100f, 100f, 110f
            };

            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setHorizontalAlignment(HorizontalAlignment.CENTER);

            // ===== Header Row =====
            String[] headers = {
                    "Contract No", "Contract Date", "Weaver", "Trader", "Quality",
                    "Quantity", "Beams", "Job Rate", "Payment Days",
                    "Production Schedule", "Machines", "Remark",
                    "Cut Length", "Minimum Delivery", "Rolling/Folding", "Created At"
            };

            for (String header : headers) {
                table.addHeaderCell(
                        new Cell()
                                .add(new Paragraph(header)
                                        .setFont(boldFont)
                                        .setFontSize(8))
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                );
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            // ===== Data Rows =====
            for (gen_bill bill : list) {

                Integer qty = bill.getQuantityMeters();
                Double rate = bill.getJobRate();

                table.addCell(getCell(String.valueOf(bill.getContractNo()), normalFont));

                table.addCell(getCell(
                        bill.getContractDate() != null
                                ? bill.getContractDate().format(dateFormatter)
                                : "-", normalFont));

                table.addCell(getCell(bill.getWeaverName(), normalFont));
                table.addCell(getCell(bill.getTraderName(), normalFont));
                table.addCell(getCell(bill.getQuality(), normalFont));
                table.addCell(getCell(qty != null ? qty.toString() : "0", normalFont));
                table.addCell(getCell(bill.getBeams() != null ? bill.getBeams().toString() : "0", normalFont));
                table.addCell(getCell(rate != null ? rate.toString() : "0", normalFont));
                table.addCell(getCell(bill.getPaymentDays() != null ? bill.getPaymentDays().toString() : "-", normalFont));

                // ✅ Production Schedule
                table.addCell(getCell(bill.getProductionSchedule(), normalFont));

                // ✅ Machines (THIS WAS MISSING)
                table.addCell(getCell(
                        bill.getNoOfMachines() != null ? bill.getNoOfMachines().toString() : "-", normalFont));

                // ✅ Remark
                table.addCell(getCell(bill.getRemark(), normalFont));

                table.addCell(getCell(bill.getCutLength(), normalFont));
                table.addCell(getCell(bill.getMinimumDelivery(), normalFont));
                table.addCell(getCell(bill.getRollingFolding(), normalFont));

                table.addCell(getCell(
                        bill.getCreatedAt() != null
                                ? bill.getCreatedAt().format(dateTimeFormatter)
                                : "-", normalFont));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private Cell getCell(String text, PdfFont font) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "-")
                        .setFont(font)
                        .setFontSize(7))
                .setTextAlignment(TextAlignment.LEFT);
    }
}
