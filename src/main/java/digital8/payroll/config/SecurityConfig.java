package digital8.payroll.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http 
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()

            .requestMatchers("/", "/index", "/forgotPassword").permitAll()

            .anyRequest().authenticated()
        )

        .formLogin(form -> form
            .loginPage("/index")
            .permitAll()
            .defaultSuccessUrl("/admin/home", true)
        )

        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/index")
            .permitAll()
        )
        .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
