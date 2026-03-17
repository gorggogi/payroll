package digital8.payroll;

import java.util.Locale; // Add this import
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import jakarta.annotation.PostConstruct;

@EnableAsync
@SpringBootApplication
public class PayrollApplication {

    @PostConstruct
    public void init() {
        // Keep your existing timezone setting
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
        
        // Force the application-wide locale to US to fix decimal/comma formatting
        Locale.setDefault(Locale.US); 
        
        // Alternatively, for the Philippines, you can use:
        // Locale.setDefault(new Locale("en", "PH"));
    }

    public static void main(String[] args) {
        SpringApplication.run(PayrollApplication.class, args);
    }
}