package digital8.payroll;
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
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Manila"));
    }

    public static void main(String[] args) {
        SpringApplication.run(PayrollApplication.class, args);
    }
}
