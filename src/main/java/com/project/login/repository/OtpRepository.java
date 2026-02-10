package com.project.login.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.login.entity.EmailOtp;
public interface OtpRepository extends JpaRepository<EmailOtp, Long> {

    EmailOtp findTopByEmailAndOtpAndUsedFalseOrderByIdDesc(String email, String otp);
}