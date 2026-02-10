package com.project.login.controller;

import com.project.login.entity.gen_bill;
import com.project.login.service.JobContractPdfService;
import com.project.login.service.JobContractService;
import com.project.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final JobContractService jobContractService;
    private final JobContractPdfService jobContractPdfService;

    public ReportController(
            JobContractService jobContractService,
            JobContractPdfService jobContractPdfService
    ) {
        this.jobContractService = jobContractService;
        this.jobContractPdfService = jobContractPdfService;
    }

    /* ==========================
       DELETE
       ========================== */
    @PostMapping("/delete/{userId}/{contractNo}")
    public String deleteJobContract(
            @PathVariable Long userId,
            @PathVariable int contractNo
    ) {
        jobContractService.deleteByUserIdAndContractNo(userId, contractNo);
        return "redirect:/report";
    }

    /* ==========================
       REPORT PAGE
       ========================== */
    @GetMapping
    public String reportPage(
            @RequestParam(required = false) String weaverName,
            @RequestParam(required = false) String traderName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            Model model
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String userName = userDetails.getName();

        List<gen_bill> reports = jobContractService.searchReportsByUser(
                userName,
                weaverName,
                traderName,
                fromDate,
                toDate
        );

        model.addAttribute("reports", reports);
        model.addAttribute("weaverName", weaverName);
        model.addAttribute("traderName", traderName);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        return "report";
    }

    /* ==========================
       EXCEL EXPORT
       ========================== */
    @GetMapping("/excel")
    public void exportExcel(
            @RequestParam(required = false) String weaverName,
            @RequestParam(required = false) String traderName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            HttpServletResponse response
    ) throws IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String userName = userDetails.getName();

        List<gen_bill> data = jobContractService.searchReportsByUser(
                userName,
                weaverName,
                traderName,
                fromDate,
                toDate
        );

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Job Contract Report");

        Row header = sheet.createRow(0);
        String[] columns = {
                "Contract No", "Contract Date", "Weaver", "Trader", "Quality",
                "Quantity", "Beams", "Job Rate", "Payment Days",
                "Production Schedule", "Machines", "Remark",
                "Cut Length", "Minimum Delivery", "Rolling/Folding", "Created At"
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        int rowNum = 1;
        for (gen_bill g : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(g.getContractNo());
            row.createCell(1).setCellValue(String.valueOf(g.getContractDate()));
            row.createCell(2).setCellValue(g.getWeaverName());
            row.createCell(3).setCellValue(g.getTraderName());
            row.createCell(4).setCellValue(g.getQuality());
            row.createCell(5).setCellValue(g.getQuantityMeters());
            row.createCell(6).setCellValue(g.getBeams());
            row.createCell(7).setCellValue(g.getJobRate());
            row.createCell(8).setCellValue(g.getPaymentDays());
            row.createCell(9).setCellValue(g.getProductionSchedule());
            row.createCell(10).setCellValue(g.getNoOfMachines());
            row.createCell(11).setCellValue(g.getRemark());
            row.createCell(12).setCellValue(g.getCutLength());
            row.createCell(13).setCellValue(g.getMinimumDelivery());
            row.createCell(14).setCellValue(g.getRollingFolding());
            row.createCell(15).setCellValue(String.valueOf(g.getCreatedAt()));
        }

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=job_contract_report.xlsx"
        );

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /* ==========================
       PDF EXPORT
       ========================== */
    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> exportPdf(
            @RequestParam(required = false) String weaverName,
            @RequestParam(required = false) String traderName,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String userName = userDetails.getName();

        List<gen_bill> data = jobContractService.searchReportsByUser(
                userName,
                weaverName,
                traderName,
                fromDate,
                toDate
        );

        ByteArrayInputStream pdf =
                jobContractPdfService.export(data);

        HttpHeaders headers = new HttpHeaders();
        headers.add(
                "Content-Disposition",
                "attachment; filename=job_contract_report.pdf"
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}
