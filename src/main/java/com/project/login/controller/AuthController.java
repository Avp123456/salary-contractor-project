package com.project.login.controller;

import com.project.login.entity.Contractor;
import com.project.login.entity.Employee;
import com.project.login.service.ContractorService;
import com.project.login.service.EmployeeExcelService;
import com.project.login.service.EmployeeService;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private ContractorService contractorService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeExcelService excelService;

    @GetMapping("/")
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
                return "redirect:/employee/dashboard";
            } else {
                model.addAttribute("error", "Invalid employee credentials");
                return "login";
            }
        }
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

        if (contractor == null) {
            return "redirect:/";
        }

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

        if (contractor == null) {
            return "redirect:/";
        }

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
    public String editEmployee(@PathVariable Long id, Model model, HttpSession session) {

        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");

        if (contractor == null) {
            return "redirect:/";
        }

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

    @PostMapping("/contractor/upload-excel")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                             @RequestParam(defaultValue = "0") int page,
                             Model model,
                             HttpSession session) {

        List<List<String>> allData = new ArrayList<>();
        List<String> headers = new ArrayList<>();

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow1 = sheet.getRow(13);
            Row headerRow2 = sheet.getRow(14);

            int totalCols = headerRow1.getLastCellNum();

            // ✅ Read headers
            for (int i = 0; i < totalCols; i++) {
                String h1 = getCellValue(headerRow1.getCell(i));
                String h2 = headerRow2 != null ? getCellValue(headerRow2.getCell(i)) : "";
                headers.add((h1 + " " + h2).trim());
            }

            // ✅ Read data rows
            for (int r = 15; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                List<String> rowData = new ArrayList<>();
                for (int i = 0; i < totalCols; i++) {
                    rowData.add(getCellValue(row.getCell(i)));
                }
                allData.add(rowData);
            }

            workbook.close();

            List<List<String>> cleanedData = new ArrayList<>();

            int empNoIndex = -1;
            int empNameIndex = -1;
            int srNoIndex = -1;

            // ✅ Find indexes
            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i).toLowerCase();

                if (h.contains("employee no")) empNoIndex = i;
                if (h.contains("name")) empNameIndex = i;

                if (h.contains("sr") && h.contains("no")) {
                    srNoIndex = i;
                }
            }

            // ✅ Clean rows
            for (List<String> row : allData) {

                // 🔥 REMOVE only if Sr No column EXACTLY "Group Total :"
                if (srNoIndex != -1 && row.size() > srNoIndex) {
                    String srValue = row.get(srNoIndex);

                    if (srValue != null) {
                        String trimmed = srValue.trim();

                        // ✅ STRICT MATCH (case-insensitive)
                        if (trimmed.equalsIgnoreCase("Group Total :")) {
                            continue; // remove only this row
                        }
                    }
                }

                // 🔥 CHECK if row is TOTAL row (any column contains "total")
                boolean isTotalRow = false;
                for (String cell : row) {
                    if (cell != null && cell.toLowerCase().contains("total")) {
                        isTotalRow = true;
                        break;
                    }
                }

                // ✅ Skip empty employee rows BUT keep total rows
                String empNo = (empNoIndex != -1 && row.size() > empNoIndex)
                        ? row.get(empNoIndex) : "";

                String empName = (empNameIndex != -1 && row.size() > empNameIndex)
                        ? row.get(empNameIndex) : "";

                if (!isTotalRow) {
                    if ((empNo == null || empNo.trim().isEmpty()) &&
                        (empName == null || empName.trim().isEmpty())) {
                        continue;
                    }
                }

                cleanedData.add(row);
            }

            allData = cleanedData;

            // ✅ COLUMN FILTER
            List<Integer> columnsToKeep = new ArrayList<>();

            for (int col = 0; col < headers.size(); col++) {

                String header = headers.get(col).toLowerCase();

                if (header.contains("designation") ||
                    header.contains("joining date") ||
                    header.contains("joining")) {
                    continue;
                }

                boolean isEmpty = true;

                for (List<String> row : allData) {
                    if (col < row.size()) {
                        String val = row.get(col);
                        if (val != null && !val.trim().isEmpty()) {
                            isEmpty = false;
                            break;
                        }
                    }
                }

                // ✅ KEEP column if ANY value exists
                if (!isEmpty) {
                    columnsToKeep.add(col);
                }
            }

            // ✅ Apply filtered columns
            List<String> filteredHeaders = new ArrayList<>();
            for (int col : columnsToKeep) {
                filteredHeaders.add(headers.get(col));
            }

            List<List<String>> filteredData = new ArrayList<>();

            for (List<String> row : allData) {
                List<String> newRow = new ArrayList<>();
                for (int col : columnsToKeep) {
                    newRow.add(col < row.size() ? row.get(col) : "");
                }
                filteredData.add(newRow);
            }

            // ✅ Store in session
            session.setAttribute("headers", filteredHeaders);
            session.setAttribute("fullData", filteredData);

            return getPaginatedData(page, model, session);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to process Excel file: " + e.getMessage());
            return "redirect:/contractor/dashboard";
        }
    }

   @GetMapping("/contractor/reports")
public String getPaginatedData(@RequestParam(defaultValue = "0") int page,
                              Model model,
                              HttpSession session) {

    List<List<String>> allData =
            (List<List<String>>) session.getAttribute("fullData");

    List<String> headers =
            (List<String>) session.getAttribute("headers");

    // ✅ FIX: handle null safely
    if (allData == null || headers == null) {
        model.addAttribute("headers", new ArrayList<>());
        model.addAttribute("data", new ArrayList<>());
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 0);
        return "contractor/reports";
    }

    int pageSize = 10;
    int start = page * pageSize;
    int end = Math.min(start + pageSize, allData.size());

    List<List<String>> paginatedData =
            allData.isEmpty() ? new ArrayList<>() : allData.subList(start, end);

    int totalPages =
            allData.isEmpty() ? 0 : (int) Math.ceil((double) allData.size() / pageSize);

    model.addAttribute("headers", headers);
    model.addAttribute("data", paginatedData);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", totalPages);

    return "contractor/reports";
}

    private String getCellValue(Cell cell) {

        if (cell == null) return "";

        switch (cell.getCellType()) {

            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();

            default:
                return "";
        }
    }

    @PostMapping("/contractor/save-excel")
public String saveExcel(HttpSession session) {

    List<List<String>> data =
            (List<List<String>>) session.getAttribute("fullData");

    List<String> headers =
            (List<String>) session.getAttribute("headers");

    if (data == null || data.isEmpty()) {
        return "redirect:/contractor/reports";
    }

    // ✅ FIXED
    excelService.saveAll(headers, data);

    return "redirect:/contractor/reports";
}
}
