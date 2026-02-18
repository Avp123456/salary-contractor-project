package com.project.login.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("ShreeGuru Software Solution - OTP Verification");

            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; background-color: #f4f6f9; padding: 20px;'>"
                            + "<div style='max-width: 600px; margin: auto; background: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"

                            + "<div style='background: linear-gradient(90deg,#6a11cb,#2575fc); padding: 30px; text-align: center; color: white;'>"
                            + "<h2 style='margin:0;'>ShreeGuru Software Solution</h2>"
                            + "<p style='margin:5px 0 0;'>Secure OTP Verification</p>"
                            + "</div>"

                            + "<div style='padding: 30px; text-align: center;'>"
                            + "<p style='font-size: 16px;'>Use the below One Time Password (OTP) to continue:</p>"

                            + "<div style='display: inline-block; margin: 20px 0; padding: 15px 30px; "
                            + "font-size: 28px; font-weight: bold; letter-spacing: 5px; "
                            + "background: #f0f4ff; border: 2px dashed #2575fc; "
                            + "border-radius: 8px; color: #2575fc;'>"
                            + otp
                            + "</div>"

                            + "<p style='color: #555;'>This OTP is valid for <strong>5 minutes</strong>.</p>"
                            + "<p style='font-size: 13px; color: #888;'>If you did not request this, please ignore this email.</p>"
                            + "</div>"

                            + "<div style='background:#f4f6f9; padding:15px; text-align:center; font-size:12px; color:#999;'>"
                            + "© 2026 ShreeGuru Software Solution | System Generated Email"
                            + "</div>"

                            + "</div>"
                            + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send OTP email");
        }
    }
}
