package com.project.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.login.entity.User;
import com.project.login.repository.UserRepository;
import com.project.login.service.OtpService;

import jakarta.servlet.http.HttpSession;

@Controller
public class RegisterController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserRepository userRepo;

    /* STEP 1: Send OTP */
    @PostMapping("/send-register-otp")
    public String sendOtp(HttpSession session,
                          @RequestParam String name,
                          @RequestParam String email,
                          @RequestParam String password,
                          Model model) {
    	    System.out.println("SEND OTP CONTROLLER HIT");

        // store user data temporarily in session
        session.setAttribute("REG_NAME", name);
        session.setAttribute("REG_EMAIL", email);
        session.setAttribute("REG_PASSWORD", password);

        otpService.sendOtp(email);

        model.addAttribute("otpSent", true); // ✅ REQUIRED
        return "register"; // stay on register page
    }

    /* STEP 2: Verify OTP and Save User */
    @PostMapping("/verify-register-otp")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            Model model) {

        String email = (String) session.getAttribute("REG_EMAIL");

        if (email == null) {
            return "redirect:/register";
        }

        boolean valid = otpService.verifyOtp(email, otp);

        if (!valid) {
            model.addAttribute("otpSent", true); // keep OTP box visible
            model.addAttribute("error", "Invalid or expired OTP");
            return "register";
        }

        // OTP verified → save user
        User user = new User();
        user.setName((String) session.getAttribute("REG_NAME"));
        user.setEmail(email);
        user.setPassword((String) session.getAttribute("REG_PASSWORD"));

        userRepo.save(user);

        session.invalidate(); // clear temp data

        return "redirect:/"; // login page
    }
}
