package com.social.Service.imp;

import com.social.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class EmailServiceImp implements EmailService {



    @Autowired
    private JavaMailSender mailSender;
    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("Verify Your Social Media Account Registration");
        message.setText("Welcome to our platform!\n\n" +
                "Your verification OTP code is: " + otp + "\n\n" +
                "This code is confidential and will expire in 5 minutes.");

        mailSender.send(message);
    }
}
