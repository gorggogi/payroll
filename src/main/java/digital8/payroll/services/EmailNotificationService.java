package digital8.payroll.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailNotificationService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailjetEmailClient mailjetEmailClient;

    @Async 
    public void sendSetupEmail(String toEmail, String firstName, String token) {
        Context context = new Context();
        context.setVariable("name", firstName);

        String setupLink = baseUrl + "/setup-password?token=" + token;
        context.setVariable("setupLink", setupLink);

        String htmlBody = templateEngine.process("html/email/setup-email", context);

        String subject = "Welcome to Digital8 - Set up your account";
        mailjetEmailClient.sendHtmlEmail(
                "digi8.payroll.system@gmail.com",
                "Digital8 Payroll",
                toEmail,
                firstName,
                subject,
                htmlBody
        );
        System.out.println("Async Email sent successfully to: " + toEmail);
    }
}