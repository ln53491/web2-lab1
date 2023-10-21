package com.web.lab1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers("/").permitAll()
                .requestMatchers("/css/*").permitAll()
                .anyRequest().authenticated());
        http.logout(logout -> {
            logout.logoutSuccessUrl("/").deleteCookies("JSESSIONID").invalidateHttpSession(true);
            SecurityContextHolder.clearContext();
        });
        http.oauth2Login(withDefaults());
        return http.build();
    }
}