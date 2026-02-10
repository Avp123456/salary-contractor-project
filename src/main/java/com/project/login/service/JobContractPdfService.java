package com.project.login.service;

import com.project.login.entity.gen_bill;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class JobContractPdfService {

    public ByteArrayInputStream export(List<gen_bill> list) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        // ===== Title =====
        document.add(new Paragraph("Job Contract Report").setBold());
        document.add(new Paragraph(" "));

        // ===== Table =====
        Table table = new Table(10);

        table.addHeaderCell("Contract No");
        table.addHeaderCell("Contract Date");
        table.addHeaderCell("Weaver");
        table.addHeaderCell("Trader");
        table.addHeaderCell("Quality");
        table.addHeaderCell("Quantity (Mtrs)");
        table.addHeaderCell("Beams");
        table.addHeaderCell("Job Rate");
        table.addHeaderCell("Amount");
        table.addHeaderCell("Cut Length");

        for (gen_bill bill : list) {

            table.addCell(String.valueOf(bill.getContractNo()));

            table.addCell(
                    bill.getContractDate() != null
                            ? bill.getContractDate().toString()
                            : "-"
            );

            table.addCell(
                    bill.getWeaverName() != null
                            ? bill.getWeaverName()
                            : "-"
            );

            table.addCell(
                    bill.getTraderName() != null
                            ? bill.getTraderName()
                            : "-"
            );

            table.addCell(
                    bill.getQuality() != null
                            ? bill.getQuality()
                            : "-"
            );

            Integer qty = bill.getQuantityMeters();
            Double rate = bill.getJobRate();

            table.addCell(qty != null ? qty.toString() : "0");
            table.addCell(bill.getBeams() != null ? bill.getBeams().toString() : "0");
            table.addCell(rate != null ? rate.toString() : "0");

            double amount =
                    (qty != null && rate != null)
                            ? qty * rate
                            : 0;

            table.addCell(String.valueOf(amount));

            table.addCell(
                    bill.getCutLength() != null
                            ? bill.getCutLength()
                            : "-"
            );
        }

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
