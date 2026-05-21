package com.social.Service.imp;

import com.social.Service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import sendinblue.ApiClient;
import sendinblue.auth.ApiKeyAuth;
import sendinblue.ApiException;

import java.util.Collections;

@Service
public class EmailServiceImp implements EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        // 1. Instantiate a new, isolated client instance
        ApiClient client = new ApiClient();

        // 2. Safely fetch and apply the authentication configuration
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) client.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);

        // 3. Pass the customized client directly into the API Instance constructor
        TransactionalEmailsApi apiInstance = new TransactionalEmailsApi(client);
        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();

        // 4. Configure sender and payload details
        sendSmtpEmail.setSender(new SendSmtpEmailSender().email("samantasurajit533@gmail.com").name("Social App"));
        sendSmtpEmail.setTo(Collections.singletonList(new SendSmtpEmailTo().email(toEmail)));
        sendSmtpEmail.setSubject("Your Verification OTP");
        sendSmtpEmail.setHtmlContent("<h3>Welcome! SnapTalk</h3><p>Your OTP is: <b>" + otp + "</b></p>");

        try {
            apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("OTP successfully sent to " + toEmail);
        } catch (ApiException e) {
            System.err.println("Brevo API Error: " + e.getResponseBody());
            throw new RuntimeException("Brevo API Error: " + e.getResponseBody());
        } catch (Exception e) {
            throw new RuntimeException("Email Failed: " + e.getMessage());
        }
    }
}
