package com.project.login.controller;

import com.project.login.entity.User;
import com.project.login.service.UserService;
import com.project.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class AuthController {

    @Autowired
    private UserService service;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/login")
    public String loginRedirect() {
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // ✅ SIMPLE REGISTER (NO OTP)
    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {

        if (service.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already exists!");
            return "register";
        }

        service.saveUser(user);

        // ✅ SUCCESS MESSAGE
        model.addAttribute("success", "Registered Successfully!");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return "redirect:/";
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        User user = service.findByEmail(userDetails.getUsername()).get();

        // ✅ ROLE BASED REDIRECTION
        if (user.getRole() == com.project.login.entity.Role.CONTRACTOR) {
            return "contractor/dashboard";
        } else {
            return "employee/dashboard";
        }
    }
}