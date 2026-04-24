package com.project.login.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.login.entity.Contractor;
import com.project.login.entity.Employee;
import com.project.login.entity.UploadedFileColumns;
import com.project.login.entity.UploadedFileData;
import com.project.login.entity.UploadedFiles;
import com.project.login.repository.UploadedFileColumnsRepository;
import com.project.login.repository.UploadedFileDataRepository;
import com.project.login.repository.UploadedFileRepository;
import com.project.login.service.ContractorService;
import com.project.login.service.EmployeeService;
import com.project.login.service.ExcelService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private ContractorService contractorService;

    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private UploadedFileRepository fileRepo;

    @Autowired
    private UploadedFileColumnsRepository columnRepo;

    @Autowired
    private UploadedFileDataRepository dataRepo;

    @Autowired
    private ExcelService excelService;

    private Long getCurrentContractorId(HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        return (contractor != null) ? contractor.getContractorId() : null;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            Model model,
            HttpSession session) {

        if (role.equalsIgnoreCase("CONTRACTOR")) {

            Contractor contractor = contractorService.login(email, password);

            if (contractor != null) {
                session.setAttribute("loggedInContractor", contractor);
                return "redirect:/contractor/dashboard";
            } else {
                model.addAttribute("error", "Invalid contractor credentials");
                return "login";
            }

        } else {

            Employee employee = employeeService.login(email, password);

            if (employee != null) {
                session.setAttribute("loggedInEmployee", employee);
                return "redirect:/employee/dashboard";
            } else {
                model.addAttribute("error", "Invalid employee credentials");
                return "login";
            }
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?logout";
    }

    @GetMapping("/contractor/dashboard")
    public String contractorDashboard(Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        if (contractorId == null) return "redirect:/login";

        List<UploadedFileData> allData = dataRepo.findByContractorId(contractorId);
        long totalEmployees = 0;
        double totalSalary = 0.0;

        // Group data by fileId to find the correct columns for each file
        java.util.Map<Long, List<UploadedFileData>> dataByFile = allData.stream()
            .collect(java.util.stream.Collectors.groupingBy(UploadedFileData::getFileId));

        for (java.util.Map.Entry<Long, List<UploadedFileData>> entry : dataByFile.entrySet()) {
            Long fileId = entry.getKey();
            List<UploadedFileData> fileRows = entry.getValue();

            List<UploadedFileColumns> cols = columnRepo.findByFileId(fileId);
            if (cols.isEmpty()) continue;

            // Find the ID column (to identify valid employee rows)
            UploadedFileColumns idCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE") || name.contains("EMP");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);

            // Find the Salary column (the last mapped column)
            UploadedFileColumns salaryCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .max(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);

            if (salaryCol != null && salaryCol.getActualColumn() != null) {
                String idGetter = (idCol != null) ? "get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1) : null;
                String salaryGetter = "get" + salaryCol.getActualColumn().substring(0, 1).toUpperCase() + salaryCol.getActualColumn().substring(1);

                for (UploadedFileData row : fileRows) {
                    try {
                        // Check if it's a valid employee row (ID is not empty)
                        boolean isValidRow = true;
                        if (idGetter != null) {
                            java.lang.reflect.Method mId = UploadedFileData.class.getMethod(idGetter);
                            Object idVal = mId.invoke(row);
                            if (idVal == null || idVal.toString().trim().isEmpty()) {
                                isValidRow = false;
                            }
                        }

                        if (isValidRow) {
                            totalEmployees++;
                            java.lang.reflect.Method mSal = UploadedFileData.class.getMethod(salaryGetter);
                            Object salVal = mSal.invoke(row);
                            if (salVal != null) {
                                if (salVal instanceof Double) {
                                    totalSalary += (Double) salVal;
                                } else {
                                    String strVal = salVal.toString().replace(",", "").trim();
                                    if (!strVal.isEmpty()) {
                                        totalSalary += Double.parseDouble(strVal);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {}
                }
            }
        }

        model.addAttribute("totalEmployees", totalEmployees);
        model.addAttribute("totalSalary", totalSalary);
        model.addAttribute("totalFiles", fileRepo.findByContractorId(contractorId).size());
        return "contractor/dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "employee/dashboard";
    }

    @GetMapping("/contractor/employees")
    public String employees(Model model, HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        List<Employee> list = employeeService.getByContractor(contractor);
        model.addAttribute("employees", list);
        return "contractor/employees";
    }

    @GetMapping("/contractor/add-employee")
    public String addEmployeePage(Model model) {
        model.addAttribute("employee", new Employee());
        return "contractor/add-employee";
    }

    @PostMapping("/contractor/save-employee")
    public String saveEmployee(@ModelAttribute Employee employee, HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        employee.setContractor(contractor);
        employeeService.save(employee);
        return "redirect:/contractor/employees";
    }

    @GetMapping("/contractor/delete-employee/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteById(id);
        return "redirect:/contractor/employees";
    }

    @GetMapping("/contractor/edit-employee/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {
        Employee emp = employeeService.getById(id);
        model.addAttribute("employee", emp);
        return "contractor/add-employee";
    }

    @GetMapping("/contractor/salary")
    public String salary(Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        model.addAttribute("files", fileRepo.findByContractorId(contractorId));
        return "contractor/salary";
    }

    @GetMapping("/contractor/show-salary-data")
    public String showSalaryData(@RequestParam Long fileId, Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        if (file == null || !file.getContractorId().equals(contractorId)) return "redirect:/contractor/salary";

        List<UploadedFileColumns> columns = columnRepo.findByFileId(fileId);
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));
        
        List<UploadedFileColumns> displayColumns = new java.util.ArrayList<>();
        for(UploadedFileColumns c : columns) if(c.isParse() != null && c.isParse()) displayColumns.add(c);

        List<UploadedFileData> dataList = dataRepo.findByFileId(fileId);
        List<List<String>> rows = new java.util.ArrayList<>();

        for (UploadedFileData data : dataList) {
            List<String> row = new java.util.ArrayList<>();
            for (UploadedFileColumns col : displayColumns) {
                String value = "";
                try {
                    if (col.getActualColumn().startsWith("str")) {
                        java.lang.reflect.Method method = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                        Object val = method.invoke(data);
                        value = val != null ? val.toString() : "";
                    } else if (col.getActualColumn().startsWith("num")) {
                        java.lang.reflect.Method method = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                        Object val = method.invoke(data);
                        if (val != null) {
                            Double d = (Double) val;
                            if (d == d.longValue()) value = String.format("%d", d.longValue());
                            else value = String.format("%s", d);
                        }
                    }
                } catch (Exception e) {}
                row.add(value);
            }
            rows.add(row);
        }

        model.addAttribute("files", fileRepo.findByContractorId(contractorId));
        model.addAttribute("selectedFileId", fileId);
        model.addAttribute("columns", displayColumns);
        model.addAttribute("rows", rows);
        return "contractor/salary";
    }

    @GetMapping("/contractor/payments")
    public String payments(Model model, HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        if (contractor == null) return "redirect:/login";
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

            // Find ID and Salary columns
            UploadedFileColumns idCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .filter(c -> {
                    String name = c.getColumnName().toUpperCase();
                    return name.contains("ID") || name.contains("CODE") || name.contains("EMP");
                })
                .min(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);

            UploadedFileColumns salaryCol = cols.stream()
                .filter(c -> c.isParse() != null && c.isParse())
                .max(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition))
                .orElse(null);

            if (idCol != null && salaryCol != null) {
                String idGetter = "get" + idCol.getActualColumn().substring(0, 1).toUpperCase() + idCol.getActualColumn().substring(1);
                String salaryGetter = "get" + salaryCol.getActualColumn().substring(0, 1).toUpperCase() + salaryCol.getActualColumn().substring(1);

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
                                java.lang.reflect.Method mSal = UploadedFileData.class.getMethod(salaryGetter);
                                Object salVal = mSal.invoke(row);
                                Double amount = 0.0;
                                if (salVal != null) {
                                    if (salVal instanceof Double) amount = (Double) salVal;
                                    else amount = Double.parseDouble(salVal.toString().replace(",", "").trim());
                                }

                                java.util.Map<String, Object> p = new java.util.HashMap<>();
                                p.put("id", row.getId()); // Store the data ID
                                p.put("empCode", match.getEmpCode());
                                p.put("name", match.getName());
                                p.put("amount", amount);
                                p.put("status", row.getStatus() != null ? row.getStatus() : "Pending");
                                p.put("structureViewed", row.getStructureViewed() != null ? row.getStructureViewed() : false);
                                p.put("payslipGenerated", row.getPayslipGenerated() != null ? row.getPayslipGenerated() : false);
                                paymentList.add(p);
                            }
                        }
                    } catch (Exception e) {}
                }
            }
        }

        model.addAttribute("payments", paymentList);
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
        
        List<java.util.Map<String, String>> fields = new java.util.ArrayList<>();
        for (UploadedFileColumns col : columns) {
            if (col.isParse() != null && col.isParse()) {
                java.util.Map<String, String> f = new java.util.HashMap<>();
                f.put("name", col.getColumnName());
                f.put("actual", col.getActualColumn());
                f.put("type", col.getDataType());
                
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

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("id", data.getId());
        res.put("fields", fields);
        return res;
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
        return "OK";
    }

    @GetMapping("/contractor/payslip/{id}")
    public String viewPayslip(@PathVariable Long id, Model model, HttpSession session) {
        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");
        if (contractor == null) return "redirect:/login";
        Long contractorId = contractor.getContractorId();

        UploadedFileData data = dataRepo.findById(id).orElse(null);
        if (data == null || !data.getContractorId().equals(contractorId)) return "redirect:/contractor/payments";

        UploadedFiles file = fileRepo.findById(data.getFileId()).orElse(null);
        if (file == null) return "redirect:/contractor/payments";

        List<UploadedFileColumns> columns = columnRepo.findByFileId(data.getFileId());
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        String employeeCode = "";
        String employeeName = "";
        Double totalAmount = 0.0;
        Double totalEarnings = 0.0;
        Double totalDeductions = 0.0;

        List<java.util.Map<String, Object>> components = new java.util.ArrayList<>();

        for (UploadedFileColumns col : columns) {
            if (col.isParse() != null && col.isParse()) {
                String colName = col.getColumnName().toUpperCase();
                boolean isId = colName.contains("ID") || colName.contains("CODE") || colName.contains("EMP");
                boolean isName = colName.contains("NAME");
                
                String valStr = "";
                Double valNum = 0.0;
                boolean isNumber = false;
                
                try {
                    java.lang.reflect.Method m = UploadedFileData.class.getMethod("get" + col.getActualColumn().substring(0, 1).toUpperCase() + col.getActualColumn().substring(1));
                    Object result = m.invoke(data);
                    if (result != null) {
                        valStr = result.toString().trim();
                        if (result instanceof Double) {
                            valNum = (Double) result;
                            isNumber = true;
                        } else if (col.getActualColumn().startsWith("num")) {
                            valNum = Double.parseDouble(valStr.replace(",", ""));
                            isNumber = true;
                        } else {
                            // Try parsing string as number to ensure totals work
                            try {
                                valNum = Double.parseDouble(valStr.replace(",", ""));
                                isNumber = true;
                            } catch (Exception ex) {
                                isNumber = false;
                            }
                        }
                    }
                } catch (Exception e) {}

                if (isId && employeeCode.isEmpty()) employeeCode = valStr;
                else if (isName && employeeName.isEmpty()) employeeName = valStr;
                else {
                    java.util.Map<String, Object> comp = new java.util.HashMap<>();
                    comp.put("name", col.getColumnName());
                    
                    if (isNumber) {
                        comp.put("value", valNum);
                        comp.put("isString", false);
                    } else {
                        comp.put("value", valStr);
                        comp.put("isString", true);
                    }
                    
                    if (colName.contains("DEDUCT") || colName.contains("TDS") || colName.contains("TAX") || colName.contains("PF") || colName.contains("FINE") || colName.contains("FUND") || colName.contains("ESI") || colName.contains("LOAN") || colName.contains("PROF")) {
                        comp.put("type", "Deductions");
                        if (isNumber && !colName.contains("TOTAL")) totalDeductions += valNum;
                    } else if (colName.contains("TOTAL") || colName.contains("PAYABLE") || colName.contains("NET") || colName.contains("AMOUNT")) {
                        comp.put("type", "Total");
                        if (isNumber) totalAmount = valNum;
                    } else {
                        comp.put("type", "Earnings");
                        if (isNumber && !colName.contains("TOTAL")) totalEarnings += valNum;
                    }
                    components.add(comp);
                }
            }
        }

        if (employeeName.isEmpty() && !employeeCode.isEmpty()) {
            final String finalEmployeeCode = employeeCode;
            Employee emp = employeeService.getByContractor(contractor).stream()
                .filter(e -> finalEmployeeCode.equalsIgnoreCase(e.getEmpCode()))
                .findFirst()
                .orElse(null);
            if (emp != null) employeeName = emp.getName();
        }

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

    @GetMapping("/contractor/profile")
    public String profile() {
        return "contractor/profile";
    }

    @PostMapping("/contractor/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, HttpSession session) {
        List<List<String>> data = excelService.parseExcel(file);
        session.setAttribute("excelData", data);

        Long contractorId = getCurrentContractorId(session);
        String originalName = file.getOriginalFilename();

        // Check if a file with the same name already exists for this contractor
        UploadedFiles existing = fileRepo.findFirstByContractorIdAndFileNameOrderByUploadDateDesc(contractorId, originalName);

        if (existing != null) {
            // Update ONLY the file bytes, size and upload date — keep all configs intact
            try { existing.setFileContent(file.getBytes()); } catch (Exception e) {}
            existing.setSize(file.getSize());
            existing.setUploadDate(java.time.LocalDateTime.now());
            fileRepo.save(existing);
        } else {
            // New file — create fresh record
            UploadedFiles fileObj = new UploadedFiles();
            fileObj.setFileName(originalName);
            fileObj.setSize(file.getSize());
            fileObj.setUploadDate(java.time.LocalDateTime.now());
            fileObj.setContractorId(contractorId);
            try { fileObj.setFileContent(file.getBytes()); } catch (Exception e) {}
            fileRepo.save(fileObj);
        }

        return "redirect:/contractor/reports";
    }
    
    @GetMapping("/contractor/reports")
    public String reports(Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        model.addAttribute("files", fileRepo.findByContractorId(contractorId));
        return "contractor/reports";
    }
    
    @PostMapping("/contractor/save-columns-and-config")
    @ResponseBody
    public String saveColumnsAndConfig(@RequestBody java.util.Map<String, Object> payload, HttpSession session) {
        Object fileIdObj = payload.get("fileId");
        if (fileIdObj == null) return "Error: Missing fileId";
        
        Long fileId = Long.valueOf(fileIdObj.toString());
        int headerCount = payload.get("headerCount") != null ? Integer.parseInt(payload.get("headerCount").toString()) : 0;
        int trailerCount = payload.get("trailerCount") != null ? Integer.parseInt(payload.get("trailerCount").toString()) : 0;
        List<java.util.Map<String, Object>> columnData = (List<java.util.Map<String, Object>>) payload.get("columns");
        
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        if (file == null || !file.getContractorId().equals(contractorId)) return "Error: Unauthorized";
        
        file.setHeaderCount(headerCount);
        file.setTrailerCount(trailerCount);
        fileRepo.save(file);

        columnRepo.deleteByFileId(fileId);
        List<UploadedFileColumns> columns = new java.util.ArrayList<>();
        int strCount = 1;
        int numCount = 1;

        for (java.util.Map<String, Object> colMap : columnData) {
            UploadedFileColumns c = new UploadedFileColumns();
            c.setFileId(fileId);
            c.setContractorId(contractorId);
            c.setColumnName(colMap.get("columnName").toString());
            c.setDataType(colMap.get("dataType").toString());
            c.setColumnPosition(Integer.parseInt(colMap.get("columnPosition").toString()));
            c.setParse(Boolean.parseBoolean(colMap.get("parse").toString()));
            
            if (c.isParse() != null && c.isParse()) {
                if ("STRING".equalsIgnoreCase(c.getDataType())) {
                    c.setActualColumn("str" + strCount++);
                } else {
                    c.setActualColumn("num" + numCount++);
                }
            }
            columns.add(c);
        }
        columnRepo.saveAll(columns);
        session.setAttribute("fileId", fileId);
        return "OK";
    }
    
    @PostMapping("/contractor/save-data")
    public String saveData(HttpSession session) {
        List<List<String>> data = (List<List<String>>) session.getAttribute("processedData");
        Long fileId = (Long) session.getAttribute("fileId");
        Long contractorId = getCurrentContractorId(session);
        List<UploadedFileColumns> columns = columnRepo.findByFileIdAndContractorId(fileId, contractorId);
        // CRITICAL: Sort columns by position to match the order in processedData
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));
        
        if (data == null && fileId != null) {
            UploadedFiles file = fileRepo.findById(fileId).orElse(null);
            if (file == null || !file.getContractorId().equals(contractorId)) return "redirect:/contractor/reports";
            List<List<String>> excelData = (List<List<String>>) session.getAttribute("excelData");
            if (excelData == null && file != null && file.getFileContent() != null) {
                excelData = excelService.parseExcel(file.getFileContent());
            }
            if (excelData != null && file != null) {
                data = new java.util.ArrayList<>();
                int start = file.getHeaderCount() != null ? file.getHeaderCount() : 0;
                int end = excelData.size() - (file.getTrailerCount() != null ? file.getTrailerCount() : 0);
                for (int i = start; i < end && i < excelData.size(); i++) {
                    List<String> row = excelData.get(i);
                    List<String> newRow = new java.util.ArrayList<>();
                    for (UploadedFileColumns col : columns) {
                        if (col.isParse() != null && col.isParse()) {
                            int pos = col.getColumnPosition() - 1;
                            String val = pos >= 0 && pos < row.size() ? row.get(pos) : "";
                            newRow.add(val);
                        }
                    }
                    boolean allEmpty = true;
                    for(String s : newRow) if(s != null && !s.trim().isEmpty()) { allEmpty = false; break; }
                    if(!allEmpty) data.add(newRow);
                }
            }
        }

        List<UploadedFileColumns> parseColumns = new java.util.ArrayList<>();
        for(UploadedFileColumns c : columns) if(c.isParse() != null && c.isParse()) parseColumns.add(c);

        // Delete existing data for this file before saving to avoid duplicates
        dataRepo.deleteByFileId(fileId);

        for (List<String> row : data) {
            UploadedFileData d = new UploadedFileData();
            d.setFileId(fileId);
            d.setContractorId(contractorId);

            for (int i = 0; i < parseColumns.size(); i++) {
                UploadedFileColumns col = parseColumns.get(i);
                String value = i < row.size() ? row.get(i) : "";
                if (col.getActualColumn().startsWith("str")) {
                    setString(d, col.getActualColumn(), value);
                } else if (col.getActualColumn().startsWith("num")) {
                    setNumber(d, col.getActualColumn(), value);
                }
            }
            dataRepo.save(d);
        }
        return "redirect:/contractor/reports";
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
    
    @GetMapping("/contractor/columns")
    public String columns(@RequestParam Long fileId, Model model, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        
        if (file == null || !file.getContractorId().equals(contractorId)) {
            return "redirect:/contractor/reports";
        }

        List<UploadedFileColumns> existingColumns = columnRepo.findByFileIdAndContractorId(fileId, contractorId);
        
        model.addAttribute("fileId", fileId);
        model.addAttribute("headerCount", file.getHeaderCount() != null ? file.getHeaderCount() : 0);
        model.addAttribute("trailerCount", file.getTrailerCount() != null ? file.getTrailerCount() : 0);
        model.addAttribute("existingColumns", existingColumns);
        
        session.setAttribute("fileId", fileId);
        return "contractor/columns";
    }
    
    
    @GetMapping("/contractor/preview")
    public String preview(Model model, HttpSession session) {
        Long fileId = (Long) session.getAttribute("fileId");
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        if (file == null || !file.getContractorId().equals(contractorId)) return "redirect:/contractor/reports";
        
        List<List<String>> excelData = (List<List<String>>) session.getAttribute("excelData");
        
        System.out.println("Preview - File ID: " + fileId);
        if (file != null) {
            System.out.println("File found in DB. Content exists? " + (file.getFileContent() != null));
        }

        // Always re-parse if we have the bytes in the DB to ensure latest logic (like comma removal) is applied
        if (file != null && file.getFileContent() != null) {
            excelData = excelService.parseExcel(file.getFileContent());
            session.setAttribute("excelData", excelData);
        }
        
        if (excelData == null) {
            return "redirect:/contractor/reports?error=Excel data not found. Please re-upload.";
        }
        
        List<UploadedFileColumns> columns = columnRepo.findByFileId(fileId);
        // Sort columns by position (Excel) so they appear in order from left to right
        columns.sort(java.util.Comparator.comparingInt(UploadedFileColumns::getColumnPosition));

        List<List<String>> filteredData = new java.util.ArrayList<>();
        if (excelData != null && file != null) {
            int start = file.getHeaderCount() != null ? file.getHeaderCount() : 0;
            int end = excelData.size() - (file.getTrailerCount() != null ? file.getTrailerCount() : 0);
            
            System.out.println("Processing file: " + file.getFileName());
            System.out.println("Excel Data Size: " + excelData.size());
            System.out.println("Start: " + start + ", End: " + end);

            for (int i = start; i < end && i < excelData.size(); i++) {
                List<String> row = excelData.get(i);
                
                // Check if the whole original row is empty
                boolean originalRowEmpty = true;
                for (String s : row) {
                    if (s != null && !s.trim().isEmpty()) {
                        originalRowEmpty = false;
                        break;
                    }
                }
                if (originalRowEmpty) continue; // Skip truly empty rows

                List<String> newRow = new java.util.ArrayList<>();
                for (UploadedFileColumns col : columns) {
                    if (col.isParse() != null && col.isParse()) {
                        int pos = col.getColumnPosition() - 1;
                        String value = pos >= 0 && pos < row.size() ? row.get(pos) : "";
                        newRow.add(value);
                    }
                }
                filteredData.add(newRow);
            }
            System.out.println("Filtered Data Rows: " + filteredData.size());
        } else {
            System.out.println("ExcelData or File is NULL. excelData null? " + (excelData == null) + " file null? " + (file == null));
        }

        List<UploadedFileColumns> displayColumns = new java.util.ArrayList<>();
        for(UploadedFileColumns c : columns) if(c.isParse() != null && c.isParse()) displayColumns.add(c);

        model.addAttribute("data", filteredData);
        model.addAttribute("columns", displayColumns);
        session.setAttribute("processedData", filteredData);
        return "contractor/preview";
    }

    @GetMapping("/contractor/delete-file/{id}")
    public String deleteFile(@PathVariable Long id, HttpSession session) {
        Long contractorId = getCurrentContractorId(session);
        UploadedFiles file = fileRepo.findById(id).orElse(null);
        if (file != null && contractorId != null && contractorId.equals(file.getContractorId())) {
            dataRepo.deleteByFileId(id);
            columnRepo.deleteByFileId(id);
            fileRepo.delete(file);
        }
        return "redirect:/contractor/reports";
    }
}
