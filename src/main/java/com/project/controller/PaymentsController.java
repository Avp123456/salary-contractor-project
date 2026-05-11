package com.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.entity.UploadedFileColumns;
import com.project.entity.UploadedFileData;
import com.project.entity.UploadedFiles;
import com.project.repository.UploadedFileColumnsRepository;
import com.project.repository.UploadedFileDataRepository;
import com.project.repository.UploadedFileRepository;
import com.project.service.EmployeeService;

import javax.servlet.http.HttpSession;
@Controller
public class PaymentsController {

	 //time stamp
    private String getTime() {
        return java.time.LocalDateTime.now().toString();
    }
    
    @Autowired
    private UploadedFileDataRepository dataRepo;
    
    @Autowired
    private UploadedFileColumnsRepository columnRepo;
    
    @Autowired
    private UploadedFileRepository fileRepo;
    
    @Autowired
    private EmployeeService employeeService;
    private Long getCurrentContractorId(HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        return (contractor != null) ? contractor.getContractorId() : null;
    }

    private UploadedFileColumns findIdColumn(List<UploadedFileColumns> cols) {
        List<UploadedFileColumns> filtered = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .collect(java.util.stream.Collectors.toList());

        // 1. Priority: Check for explicit Key column
        UploadedFileColumns specific = filtered.stream()
                .filter(c -> c.getIsKey() != null && c.getIsKey())
                .findFirst()
                .orElse(null);
        if (specific != null) return specific;

        // 2. Priority: Check for columns containing both (EMP/EMPLOYEE) and (ID/CODE)
        specific = filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return (name.contains("EMP") || name.contains("EMPLOYEE")) && (name.contains("ID") || name.contains("CODE"));
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
        if (specific != null) return specific;

        // 3. Priority: Fallback to columns containing just ID or CODE
        return filtered.stream()
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);
    }

    private Double parseAmount(Object val) {
        if (val == null) return 0.0;
        try {
            if (val instanceof Double) return (Double) val;
            String clean = val.toString().replaceAll("[^0-9.-]", "").trim();
            if (clean.isEmpty()) return 0.0;
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @GetMapping("/contractor/payments")
    public String payments(Model model, HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        if (contractor == null) return "redirect:/contractor/login";
        Long contractorId = contractor.getContractorId();

        // 1. Get all registered employees
        List<Employee> registeredEmployees = employeeService.getByContractor(contractor);
        
        // 2. Get all uploaded data
        List<UploadedFileData> allData = dataRepo.findByContractorId(contractorId);
        
        List<java.util.Map<String, Object>> paymentList = new java.util.ArrayList<>();
        
        // 3. Group data by fileId to handle different mappings
        java.util.Map<Long, List<UploadedFileData>> dataByFile = allData.stream()
            .collect(java.util.stream.Collectors.groupingBy(UploadedFileData::getFileId));

        for (java.util.Map.Entry<Long, List<UploadedFileData>> entry : dataByFile.entrySet()) {
            Long fileId = entry.getKey();
            List<UploadedFileData> fileRows = entry.getValue();

            List<UploadedFileColumns> cols = columnRepo.findByFileId(fileId);
            if (cols.isEmpty()) continue;

            // Find ID column
            UploadedFileColumns idCol = findIdColumn(cols);

            if (idCol != null) {
                String idGetter = "get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1);

                for (UploadedFileData row : fileRows) {
                    try {
                        java.lang.reflect.Method mId = UploadedFileData.class.getMethod(idGetter);
                        Object idVal = mId.invoke(row);
                        String empIdInFile = idVal != null ? idVal.toString().trim() : "";

                        if (!empIdInFile.isEmpty()) {
                            // Find matching registered employee
                            Employee match = registeredEmployees.stream()
                                .filter(e -> empIdInFile.equalsIgnoreCase(e.getEmpCode()))
                                .findFirst()
                                .orElse(null);

                            if (match != null) {
                                java.util.Map<String, Object> p = new java.util.HashMap<>();
                                p.put("id", row.getId()); // Store the data ID
                                p.put("empCode", empIdInFile);
                                p.put("name", match.getName());
                                p.put("registered", true);
                                
                                Double amount = row.getTotalAmt() != null ? row.getTotalAmt() : 0.0;
                                p.put("amount", amount);
                                p.put("status", row.getStatus() != null ? row.getStatus() : "Pending");
                                p.put("structureViewed", row.getStructureViewed() != null ? row.getStructureViewed() : false);
                                p.put("payslipGenerated", row.getPayslipGenerated() != null ? row.getPayslipGenerated() : false);
                                paymentList.add(p);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        model.addAttribute("payments", paymentList);
System.out.println("[INFO] Payments Page Visited "+getTime());
        return "contractor/payments";
    }

    @GetMapping("/contractor/get-payment-details")
    @ResponseBody
    public java.util.Map<String, Object> getPaymentDetails(@RequestParam Long id, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return null;
        
        // Mark as viewed
        if (data.getStructureViewed() == null || !data.getStructureViewed()) {
            data.setStructureViewed(true);
            dataRepo.save(data);
        }

        List<UploadedFileColumns> columns = columnRepo.findByFileId(data.getFileId());
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        List<java.util.Map<String, Object>> fields = new java.util.ArrayList<>();
        for (UploadedFileColumns col : columns) {
            if (col.isParse() != null && col.isParse()) {
                java.util.Map<String, Object> f = new java.util.HashMap<>();
                f.put("name", col.getColumnName());
                f.put("actual", col.getActualColumn());
                f.put("type", col.getDataType());
                f.put("isTotal", false);
                
                String val = "";
                try {
                    java.lang.reflect.Method m = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                    Object result = m.invoke(data);
                    if (result != null) val = result.toString();
                } catch (Exception e) {}
                f.put("value", val);
                fields.add(f);
            }
        }

        // Add the calculated total field at the very end
        java.util.Map<String, Object> totalField = new java.util.HashMap<>();
        totalField.put("name", "Total Payable Amount");
        totalField.put("value", data.getTotalAmt() != null ? String.format("%.2f", data.getTotalAmt()) : "0.00");
        totalField.put("isTotal", true);
        fields.add(totalField);

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("id", data.getId());
        res.put("fields", fields);
        return res;
    }

    
    private void setString(UploadedFileData d, String col, String val) {
        try {
            java.lang.reflect.Method method = UploadedFileData.class.getMethod("set" + col.substring(0, 1).toUpperCase() + col.substring(1), String.class);
            method.invoke(d, val);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setNumber(UploadedFileData d, String col, String val) {
        Double num = 0.0;
        try {
            num = val == null || val.isEmpty() ? 0 : Double.parseDouble(val);
            java.lang.reflect.Method method = UploadedFileData.class.getMethod("set" + col.substring(0, 1).toUpperCase() + col.substring(1), Double.class);
            method.invoke(d, num);
        } catch (Exception e) {}
    }
    
    @PostMapping("/contractor/update-payment-data")
    @ResponseBody
    public String updatePaymentData(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        Long id = Long.valueOf(payload.get("id").toString());
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "Error";

        java.util.Map<String, String> updates = (java.util.Map<String, String>) payload.get("updates");
        for (java.util.Map.Entry<String, String> entry : updates.entrySet()) {
            String col = entry.getKey();
            String val = entry.getValue();
            if (col.startsWith("str")) setString(data, col, val);
            else if (col.startsWith("num")) setNumber(data, col, val);
        }
        dataRepo.save(data);
        System.out.println("[ACTION] Payment Data Updated "+ getTime());
        return "OK";
    }

    @PostMapping("/contractor/update-payment-status")
    @ResponseBody
    public String updatePaymentStatus(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        Long id = Long.valueOf(payload.get("id").toString());
        String status = payload.get("status").toString();
        
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "Error";
        
        data.setStatus(status);
        dataRepo.save(data);
        System.out.println("[ACTION] Payment Status Updated "+ getTime());

        
        return "OK";
    }

    @PostMapping("/contractor/generate-payslip")
    @ResponseBody
    public String generatePayslipEndpoint(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        Long id = Long.valueOf(payload.get("id").toString());
        
        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "Error";
        
        data.setPayslipGenerated(true);
        dataRepo.save(data);
        System.out.println("[ACTION] Payment PaySlip Generated "+ getTime());

        return "OK";
    }

    @GetMapping("/contractor/payslip/{id}")
    public String viewPayslip(@PathVariable Long id, Model model, HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        if (contractor == null) return "redirect:/contractor/login";
        Long contractorId = contractor.getContractorId();

        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "redirect:/contractor/payments";

        UploadedFiles file = fileRepo.findById(data.getFileId()).orElse(null);
        if (file == null) return "redirect:/contractor/payments";

        List<UploadedFileColumns> columns = (data.getConfigId() != null) ? columnRepo.findByConfigId(data.getConfigId()) : columnRepo.findByFileId(data.getFileId());
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        String employeeCode = "";
        String employeeName = "";
        Double totalAmount = 0.0;
        Double totalEarnings = 0.0;
        Double totalDeductions = 0.0;

        List<java.util.Map<String, Object>> components = new java.util.ArrayList<>();

        // Source of truth for identity
        UploadedFileColumns idColLocal = findIdColumn(columns);
        if (idColLocal != null) {
            try {
                java.lang.reflect.Method m = UploadedFileData.class.getMethod("get" + idColLocal.getActualColumn().substring(0, 1).toUpperCase() + idColLocal.getActualColumn().substring(1));
                Object result = m.invoke(data);
                employeeCode = result != null ? result.toString().trim() : "";
            } catch (Exception e) {}
        }

        // Fetch all registered employees for name mapping
        List<Employee> registeredEmployees = employeeService.getByContractor(contractor);

        // Fetch Employee Name from DB if not found in file
        if (!employeeCode.isEmpty()) {
            final String finalCode = employeeCode;
            Employee emp = registeredEmployees.stream()
                .filter(e -> finalCode.equalsIgnoreCase(e.getEmpCode()))
                .findFirst()
                .orElse(null);
            if (emp != null) employeeName = emp.getName();
        }

        for (UploadedFileColumns col : columns) {
            // Only process numeric columns marked as E or D
            if (col.isParse() != null && col.isParse() && col.getActualColumn().startsWith("num")) {
                String sType = col.getSalaryType();
                if ("E".equalsIgnoreCase(sType) || "D".equalsIgnoreCase(sType)) {
                    Double valNum = 0.0;
                    try {
                        java.lang.reflect.Method m = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                        Object result = m.invoke(data);
                        valNum = parseAmount(result);
                    } catch (Exception e) {}

                    java.util.Map<String, Object> comp = new java.util.HashMap<>();
                    comp.put("name", col.getColumnName());
                    comp.put("value", valNum);
                    comp.put("isString", false);

                    if ("E".equalsIgnoreCase(sType)) {
                        comp.put("type", "Earnings");
                        totalEarnings += valNum;
                    } else {
                        comp.put("type", "Deductions");
                        totalDeductions += valNum;
                    }
                    components.add(comp);
                }
            }
        }

        // Source of truth for Net Pay
        totalAmount = data.getTotalAmt() != null ? data.getTotalAmt() : 0.0;

        model.addAttribute("contractorName", contractor.getName());
        model.addAttribute("employeeName", employeeName);
        model.addAttribute("employeeCode", employeeCode);
        
        String fileName = file.getFileName();
        String extractedMonthYear = extractMonthYearFromFileName(fileName);
        if (extractedMonthYear == null) {
            extractedMonthYear = java.time.format.DateTimeFormatter.ofPattern("MMM-yyyy").format(file.getUploadDate());
        }
        model.addAttribute("monthYear", extractedMonthYear.replace(" ", "_")); // Use underscores for safe PDF naming if needed, or keep spaces. Let's keep original for UI and replace in HTML.
        model.addAttribute("monthYearUi", extractedMonthYear);
        
        model.addAttribute("uploadDate", file.getUploadDate());
        model.addAttribute("components", components);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalEarnings", totalEarnings);
        model.addAttribute("totalDeductions", totalDeductions);
        System.out.println("[INFO] Payment Payslip Viewed "+ getTime());

        return "contractor/payslip";
    }

    private String extractMonthYearFromFileName(String fileName) {
        if (fileName == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*[-_\\s]*\\d{2,4}");
        java.util.regex.Matcher m = p.matcher(fileName);
        if (m.find()) {
            return m.group().toUpperCase().replaceAll("[-_\\s]+", " ");
        }
        return null;
    }



}
