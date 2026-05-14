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
        ApiClient defaultClient = Configuration.getDefaultApiClient();

        // Configure API key authorization: api-key
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

        // 1. Sender: Must be the email verified in your Brevo Dashboard
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email("your-email@gmail.com"));

        // 2. Receiver
        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(toEmail)));

        // 3. Subject and Content
        sendSmtpEmail.setSubject("Verification OTP");
        sendSmtpEmail.setHtmlContent("Your OTP code is: <b>" + otp + "</b>");

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
        } catch (Exception e) {
            throw new RuntimeException("Brevo API Connection Failed: " + e.getMessage());
        }
    }
}
