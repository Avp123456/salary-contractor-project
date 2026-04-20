package com.project.login.controller;

import com.project.login.entity.Contractor;
import com.project.login.entity.Employee;
import com.project.login.service.ContractorService;
import com.project.login.service.EmployeeService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpSession;


@Controller
public class AuthController {
	@Autowired
	private ContractorService contractorService;

	@Autowired
	private EmployeeService employeeService;



    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    
    
    @GetMapping("/contractor/dashboard")
    public String contractorDashboard() {
        return "contractor/dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard() {
        return "employee/dashboard";
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

            // ✅ STORE CONTRACTOR IN SESSION
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
    
 
    @GetMapping("/contractor/upload")
    public String upload() {
        return "contractor/upload";
    }

    @GetMapping("/contractor/employees")
    public String employees(Model model, HttpSession session) {

        Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");

        List<Employee> list = employeeService.getByContractor(contractor);

        model.addAttribute("employees", list);

        return "contractor/employees";
    }
    @GetMapping("/contractor/salary")
    public String salary() {
        return "contractor/salary";
    }

    @GetMapping("/contractor/payments")
    public String payments() {
        return "contractor/payments";
    }

    @GetMapping("/contractor/reports")
    public String reports() {
        return "contractor/reports";
    }

    @GetMapping("/contractor/profile")
    public String profile() {
        return "contractor/profile";
    }
 
    
    @GetMapping("/contractor/add-employee")
    public String addEmployeePage(Model model) {
        model.addAttribute("employee", new Employee());
        return "contractor/add-employee";
    }
    
  @PostMapping("/contractor/save-employee")
public String saveEmployee(@ModelAttribute Employee employee,
                           HttpSession session) {

    // ✅ GET LOGGED-IN CONTRACTOR
    Contractor contractor = (Contractor) session.getAttribute("loggedInContractor");

    // ✅ ASSIGN CONTRACTOR
    employee.setContractor(contractor);

    employeeService.save(employee);

    return "redirect:/contractor/employees";
}
}