package com.social.Service.imp;

import com.social.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import java.util.Collections;

@Service
public class EmailServiceImp implements EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("CRITICAL: BREVO_API_KEY is missing from Railway Variables!");
        }

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

        // MANDATORY FIX: Change this to the email you used to sign up for Brevo
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email("samantasurajit533@gmail.com"));

        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(toEmail)));
        sendSmtpEmail.setSubject("Verification Code: " + otp);
        sendSmtpEmail.setHtmlContent("<html><body><h1>Your OTP is: " + otp + "</h1></body></html>");

        try {
            System.out.println("Attempting to send API request to Brevo for: " + toEmail);
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("SUCCESS: OTP sent via Brevo API.");
        } catch (Exception e) {
            // This prints the FULL error including the status code (e.g., 401 Unauthorized)
            e.printStackTrace();
            throw new RuntimeException("Brevo API Connection Failed. Check Railway logs for stack trace.");
        }
    }
}