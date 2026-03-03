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

        String textBody = String.format(
                "Welcome to Digital8!%n%nHello %s,%n%nYour employee account has been created. To securely access your dashboard, please set up your permanent password using this link:%n%n%s%n%nThis link expires in 24 hours.%n%n— Digital8 Payroll",
                firstName, setupLink
        );

        String subject = "Set up your Digital8 Payroll account";
        mailjetEmailClient.sendHtmlEmailWithText(
                "digi8.payroll.system@gmail.com",
                "Digital8 Payroll",
                toEmail,
                firstName,
                subject,
                htmlBody,
                textBody
        );
        System.out.println("Async Email sent successfully to: " + toEmail);
    }
}