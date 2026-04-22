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
                session.setAttribute("loggedInEmployee", employee); // 🔥 ADD THIS LINE
                return "redirect:/employee/dashboard";
            } else {
                model.addAttribute("error", "Invalid employee credentials");
                return "login";
            }
        }
    }
 
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // ✅ destroy session
        return "redirect:/?logout"; // redirect to login page
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
public String uploadFile(@RequestParam("file") MultipartFile file,
                         HttpSession session) {

    List<List<String>> data = excelService.parseExcel(file);

    session.setAttribute("excelData", data);

    UploadedFiles fileObj = new UploadedFiles();

    fileObj.setFileName(file.getOriginalFilename());
    fileObj.setSize(file.getSize());
    fileObj.setUploadDate(java.time.LocalDateTime.now());

    fileRepo.save(fileObj);

    return "redirect:/contractor/reports";
}
    
    @GetMapping("/contractor/reports")
    public String reports(Model model) {

        model.addAttribute("files", fileRepo.findAll());

        return "contractor/reports";
    }
    
    
    
   @PostMapping("/contractor/save-columns")
@ResponseBody
public String saveColumns(@RequestBody List<UploadedFileColumns> cols,
                         HttpSession session) {

    Long fileId = (Long) session.getAttribute("fileId");

    // 🔥 ADD THIS LINE (IMPORTANT)
    columnRepo.deleteByFileId(fileId);

    for (UploadedFileColumns c : cols) {
        c.setFileId(fileId);
    }

    columnRepo.saveAll(cols);

    return "OK";
}
    
    @PostMapping("/contractor/save-data")
    public String saveData(HttpSession session) {

        List<List<String>> data =
                (List<List<String>>) session.getAttribute("excelData");

        Long fileId = (Long) session.getAttribute("fileId");

        List<UploadedFileColumns> columns =
                columnRepo.findByFileId(fileId);

        for (List<String> row : data) {

            UploadedFileData d = new UploadedFileData();
            d.setFileId(fileId);

            for (UploadedFileColumns col : columns) {

                int pos = col.getColumnPosition();
                String value = pos < row.size() ? row.get(pos) : "";

                if ("STRING".equalsIgnoreCase(col.getDataType())) {
                    setString(d, col.getActualColumn(), value);
                } else {
                    setNumber(d, col.getActualColumn(), value);
                }
            }

            dataRepo.save(d);
        }

        return "redirect:/contractor/reports";
    }
    
    
    
    private void setString(UploadedFileData d, String col, String val) {

        switch (col) {
            case "str1": d.setStr1(val); break;
            case "str2": d.setStr2(val); break;
        }
    }

    private void setNumber(UploadedFileData d, String col, String val) {

        Double num = 0.0;

        try {
            num = val == null || val.isEmpty() ? 0 : Double.parseDouble(val);
        } catch (Exception e) {}

        switch (col) {
            case "num1": d.setNum1(num); break;
            case "num2": d.setNum2(num); break;
        }
    }
    
    @GetMapping("/contractor/columns")
    public String columns(@RequestParam Long fileId, HttpSession session) {

        session.setAttribute("fileId", fileId);

        return "contractor/columns";
    }
    
    @GetMapping("/contractor/delete-file/{id}")
    public String deleteFile(@PathVariable Long id) {
        fileRepo.deleteById(id);
        return "redirect:/contractor/reports";
    }	
    
    
  @GetMapping("/contractor/preview")
public String preview(Model model, HttpSession session) {

    List<List<String>> excelData =
        (List<List<String>>) session.getAttribute("excelData");

    Long fileId = (Long) session.getAttribute("fileId");

    List<UploadedFileColumns> columns =
        columnRepo.findByFileId(fileId);

    // 🔥 FILTERED DATA
    List<List<String>> filteredData = new java.util.ArrayList<>();

    for (List<String> row : excelData) {

        List<String> newRow = new java.util.ArrayList<>();

        for (UploadedFileColumns col : columns) {

            int pos = col.getColumnPosition();

            String value = pos < row.size() ? row.get(pos) : "";

            newRow.add(value);
        }

        filteredData.add(newRow);
    }

    model.addAttribute("data", filteredData);
    model.addAttribute("columns", columns);

    return "contractor/preview";
}  
    @GetMapping("/contractor/config")
    public String config(@RequestParam Long fileId, Model model, HttpSession session) {

        session.setAttribute("fileId", fileId);
        model.addAttribute("fileId", fileId);

        return "contractor/config";
    }
    
@PostMapping("/contractor/save-config")
public String saveConfig(
        @RequestParam Long fileId,
        @RequestParam String headerExists,
        @RequestParam int headerLines,
        @RequestParam String parseHeader,
        @RequestParam String trailerExists,
        @RequestParam int trailerLines,
        @RequestParam String parseTrailer,
        HttpSession session) {

    List<List<String>> original =
            (List<List<String>>) session.getAttribute("excelData");

    // 🔥 MAKE COPY (IMPORTANT)
    List<List<String>> data = new java.util.ArrayList<>();
    for (List<String> row : original) {
        data.add(new java.util.ArrayList<>(row));
    }

    // ✅ HEADER REMOVE
    if ("YES".equals(headerExists) && "NO".equals(parseHeader)) {
        for (int i = 0; i < headerLines && !data.isEmpty(); i++) {
            data.remove(0);
        }
    }

    // ✅ TRAILER REMOVE
    if ("YES".equals(trailerExists) && "NO".equals(parseTrailer)) {

        int lastIndex = data.size() - 1;

        // find last non-empty row
        for (int i = data.size() - 1; i >= 0; i--) {
            boolean empty = true;
            for (String cell : data.get(i)) {
                if (cell != null && !cell.trim().isEmpty()) {
                    empty = false;
                    break;
                }
            }
            if (!empty) {
                lastIndex = i;
                break;
            }
        }

        for (int i = 0; i < trailerLines && lastIndex >= 0; i++) {
            data.remove(lastIndex);
            lastIndex--;
        }
        if (original == null) {
            return "redirect:/contractor/reports";
        }
    }

    // 🔥 SAVE BACK
    session.setAttribute("excelData", data);

    return "redirect:/contractor/columns?fileId=" + fileId;
}
}

