package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.entity.Contractor;
import com.project.entity.Employee;
import com.project.service.ContractorService;
import com.project.service.EmployeeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	@Autowired
	private ContractorService contractorService;

	@Autowired
	private EmployeeService employeeService;

	//time stamp
	String time = java.time.LocalDateTime.now().toString();




	//login page 
	@GetMapping("/")
	public String root() {
		return "redirect:/login";
	}

	//login page 
	@GetMapping("/login")
	public String loginPage() {
		//log
		System.out.println("[INFO] Login Page Visited  "+time );

		return "login";
	}

	//log in process
	@PostMapping("/login")
	public String login(
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam String role,
			Model model,
			HttpSession session) {


		if (role.equalsIgnoreCase("CONTRACTOR")) {
			System.out.println("[ACTION] Contractor selected");

			Contractor contractor = contractorService.login(email, password);

			if (contractor != null) {
				session.setAttribute("loggedInContractor", contractor);
				//log
				System.out.println("[INFO] Contractor Logged in  "+ time);
				return "redirect:/contractor/dashboard";
			} else {
				model.addAttribute("error", "Invalid contractor credentials");

				return "login";

			}

		} else {

			Employee employee = employeeService.login(email, password);

			if (employee != null) {
				session.setAttribute("loggedInEmployee", employee);
				System.out.println("[INFO] Employee Logged in  "+ time);
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
		System.out.println("[INFO] Contractor Logged Out  "+ time);
		return "redirect:/?logout";
	}
}
