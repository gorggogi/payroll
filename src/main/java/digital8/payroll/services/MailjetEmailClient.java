package digital8.payroll.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MailjetEmailClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final String secretKey;

    public MailjetEmailClient(
            @Value("${spring.mail.username}") String apiKey,
            @Value("${spring.mail.password}") String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public void sendHtmlEmail(String fromEmail,
                              String fromName,
                              String toEmail,
                              String toName,
                              String subject,
                              String htmlBody) {
        sendHtmlEmailWithText(fromEmail, fromName, toEmail, toName, subject, htmlBody, null);
    }

    /**
     * Send email with both HTML and optional plain-text part. Including a text part improves deliverability.
     */
    public void sendHtmlEmailWithText(String fromEmail,
                                     String fromName,
                                     String toEmail,
                                     String toName,
                                     String subject,
                                     String htmlBody,
                                     String textPart) {

        Map<String, Object> fromMap = Map.of(
                "Email", fromEmail,
                "Name", fromName != null ? fromName : fromEmail
        );
        Map<String, Object> toMap = Map.of(
                "Email", toEmail,
                "Name", toName != null ? toName : toEmail
        );

        java.util.Map<String, Object> message = new java.util.LinkedHashMap<>();
        message.put("From", fromMap);
        message.put("To", List.of(toMap));
        message.put("Subject", subject);
        message.put("HTMLPart", htmlBody);
        if (textPart != null && !textPart.isBlank()) {
            message.put("TextPart", textPart);
        }

        Map<String, Object> body = Map.of("Messages", List.of(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(apiKey, secretKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity("https://api.mailjet.com/v3.1/send", entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Mailjet API responded with status " + response.getStatusCode());
        }
    }
}

