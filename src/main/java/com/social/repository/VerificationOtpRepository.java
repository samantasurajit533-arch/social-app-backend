package com.social.repository;

import com.social.models.VerificationOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface VerificationOtpRepository extends JpaRepository<VerificationOTP, Integer> {


    Optional<VerificationOTP> findByEmailAndOtp(String email, String otp);
    @Transactional
    void deleteByEmail(String email);
}
