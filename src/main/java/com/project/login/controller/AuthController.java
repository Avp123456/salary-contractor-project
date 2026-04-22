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
    public String contractorDashboard() {
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
    public String salary() {
        return "contractor/salary";
    }

    @GetMapping("/contractor/payments")
    public String payments() {
        return "contractor/payments";
    }

    @GetMapping("/contractor/profile")
    public String profile() {
        return "contractor/profile";
    }

    @PostMapping("/contractor/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, HttpSession session) {
        List<List<String>> data = excelService.parseExcel(file);
        session.setAttribute("excelData", data);

        UploadedFiles fileObj = new UploadedFiles();
        fileObj.setFileName(file.getOriginalFilename());
        fileObj.setSize(file.getSize());
        fileObj.setUploadDate(java.time.LocalDateTime.now());

        try {
            fileObj.setFileContent(file.getBytes());
        } catch (Exception e) {}
        
        fileRepo.save(fileObj);
        return "redirect:/contractor/reports";
    }
    
    @GetMapping("/contractor/reports")
    public String reports(Model model) {
        model.addAttribute("files", fileRepo.findAll());
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
        
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        if (file != null) {
            file.setHeaderCount(headerCount);
            file.setTrailerCount(trailerCount);
            fileRepo.save(file);
        }

        columnRepo.deleteByFileId(fileId);
        List<UploadedFileColumns> columns = new java.util.ArrayList<>();
        int strCount = 1;
        int numCount = 1;

        for (java.util.Map<String, Object> colMap : columnData) {
            UploadedFileColumns c = new UploadedFileColumns();
            c.setFileId(fileId);
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
        List<UploadedFileColumns> columns = columnRepo.findByFileId(fileId);
        
        if (data == null && fileId != null) {
            // Re-generate processed data if session expired
            UploadedFiles file = fileRepo.findById(fileId).orElse(null);
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

        for (List<String> row : data) {
            UploadedFileData d = new UploadedFileData();
            d.setFileId(fileId);

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
        session.setAttribute("fileId", fileId);
        model.addAttribute("fileId", fileId);
        return "contractor/columns";
    }
    
    @GetMapping("/contractor/delete-file/{id}")
    public String deleteFile(@PathVariable Long id) {
        fileRepo.deleteById(id);
        return "redirect:/contractor/reports";
    }	
    
    @GetMapping("/contractor/preview")
    public String preview(Model model, HttpSession session) {
        Long fileId = (Long) session.getAttribute("fileId");
        UploadedFiles file = fileRepo.findById(fileId).orElse(null);
        List<List<String>> excelData = (List<List<String>>) session.getAttribute("excelData");
        
        System.out.println("Preview - File ID: " + fileId);
        if (file != null) {
            System.out.println("File found in DB. Content exists? " + (file.getFileContent() != null));
        }

        if (excelData == null && file != null && file.getFileContent() != null) {
            System.out.println("Attempting to re-parse file content...");
            excelData = excelService.parseExcel(file.getFileContent());
            System.out.println("Re-parsed Excel size: " + (excelData != null ? excelData.size() : "NULL"));
            session.setAttribute("excelData", excelData);
        }
        
        List<UploadedFileColumns> columns = columnRepo.findByFileId(fileId);

        List<List<String>> filteredData = new java.util.ArrayList<>();
        if (excelData != null && file != null) {
            int start = file.getHeaderCount() != null ? file.getHeaderCount() : 0;
            int end = excelData.size() - (file.getTrailerCount() != null ? file.getTrailerCount() : 0);
            
            System.out.println("Processing file: " + file.getFileName());
            System.out.println("Excel Data Size: " + excelData.size());
            System.out.println("Start: " + start + ", End: " + end);

            for (int i = start; i < end && i < excelData.size(); i++) {
                List<String> row = excelData.get(i);
                List<String> newRow = new java.util.ArrayList<>();
                for (UploadedFileColumns col : columns) {
                    if (col.isParse() != null && col.isParse()) {
                        int pos = col.getColumnPosition() - 1;
                        String value = pos >= 0 && pos < row.size() ? row.get(pos) : "";
                        newRow.add(value);
                    }
                }
                boolean allEmpty = true;
                for(String s : newRow) if(s != null && !s.trim().isEmpty()) { allEmpty = false; break; }
                if(!allEmpty) filteredData.add(newRow);
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
}
