package com.project.login.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.login.entity.EmailOtp;
import com.project.login.repository.OtpRepository;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepo;

    @Autowired
    private EmailService emailService;

    public void sendOtp(String email) {

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        emailOtp.setUsed(false);

        otpRepo.save(emailOtp);
        emailService.sendOtp(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {

        EmailOtp savedOtp =
            otpRepo.findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(email, otp);

        if (savedOtp == null) return false;
        if (savedOtp.getExpiryTime().isBefore(LocalDateTime.now())) return false;

        savedOtp.setUsed(true);
        otpRepo.save(savedOtp);

        return true;
    }
}
