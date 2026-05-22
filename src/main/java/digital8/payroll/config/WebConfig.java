package digital8.payroll.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/leave_attachments/**")
            .addResourceLocations("file:uploads/leave_attachments/");
        registry.addResourceHandler("/uploads/ot_attachments/**")
            .addResourceLocations("file:uploads/ot_attachments/");
    }
}
