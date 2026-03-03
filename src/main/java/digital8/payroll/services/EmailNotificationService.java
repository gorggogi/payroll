package digital8.payroll.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EmailNotificationService {

    @Value("${spring.mail.username}")
    private String baseUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Async 
    public void sendSetupEmail(String toEmail, String firstName, String token) {
        try {
         
            Context context = new Context();
            context.setVariable("name", firstName);
          
            String setupLink = baseUrl + "/setup-password?token=" + token;
            context.setVariable("setupLink", setupLink);

            String htmlBody = templateEngine.process("html/email/setup-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Digital8 - Set up your account");
            helper.setText(htmlBody, true); 
     
            helper.setFrom("no-reply@digital8.com"); 

            mailSender.send(message);
            System.out.println("Async Email sent successfully to: " + toEmail);
            
        } catch (MessagingException e) {
            System.err.println("Failed to send setup email. Error: " + e.getMessage());
        }
    }
}