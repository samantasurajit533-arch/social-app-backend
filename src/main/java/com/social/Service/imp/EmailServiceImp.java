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
        // Validation check to prevent NullPointerExceptions
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("Brevo API Key is missing in Environment Variables");
        }

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

        // FIX: Ensure this email is verified in Brevo -> Identifiers -> Senders
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email("YOUR_BREVO_REGISTERED_EMAIL@GMAIL.COM"));

        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(toEmail)));
        sendSmtpEmail.setSubject("Social App - Verification Code");
        sendSmtpEmail.setHtmlContent("<h3>Your OTP is: " + otp + "</h3><p>Valid for 5 minutes.</p>");

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("OTP successfully sent to: " + toEmail);
        } catch (Exception e) {
            // This will show the actual Brevo error in your Railway Logs
            System.err.println("Brevo Error Detail: " + e.getMessage());
            throw new RuntimeException("Brevo API Connection Failed: " + e.getMessage());
        }
    }
}
