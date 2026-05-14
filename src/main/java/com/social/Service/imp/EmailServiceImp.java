package com.social.Service.imp;

import com.social.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sendinblue.ApiException; // IMPORTANT

import java.util.Collections;

@Service
public class EmailServiceImp implements EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

        // CRITICAL: Replace with your actual Brevo-verified email address
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email("samantasurajit533@gmail.com").name("Social App"));

        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(toEmail)));
        sendSmtpEmail.setSubject("Your Verification OTP");
        sendSmtpEmail.setHtmlContent("<h3>Welcome!</h3><p>Your OTP is: <b>" + otp + "</b></p>");

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("OTP successfully sent to " + toEmail);
        } catch (ApiException e) {
            // This prints the REAL error from Brevo (e.g., Unauthorized or Forbidden)
            System.err.println("Brevo API Error: " + e.getResponseBody());
            throw new RuntimeException("Brevo API Error: " + e.getResponseBody());
        } catch (Exception e) {
            throw new RuntimeException("Email Failed: " + e.getMessage());
        }
    }
}
