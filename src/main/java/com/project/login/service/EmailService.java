package com.project.login.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("ShreeGuru Software Solution");
        message.setText(
                "Your OTP for registration is: " + otp +
                "\n\nThis OTP is valid for 5 minutes." + 
                "\n\nThis is system generated mail do not replay it."
        );

        mailSender.send(message);
    }
}
