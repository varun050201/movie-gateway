package com.vk.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests()
                .requestMatchers("/api/public/**").permitAll()  // Replaced antMatchers with requestMatchers
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Replaced antMatchers with requestMatchers
                .anyRequest().authenticated()
                .and().httpBasic();  // Using basic authentication here

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // BCrypt encoder for password hashing
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        // Define the admin user with roles
        UserDetails adminUser = User.withUsername(adminUsername)
                .password(passwordEncoder().encode(adminPassword))
                .roles("NORMAL_ADMIN", "ADMIN")
                .build();

        // Define a super admin user with higher privileges
        UserDetails superUser = User.withUsername("super" + adminUsername)
                .password(passwordEncoder().encode("super" + adminPassword))
                .roles("SUPER_ADMIN", "ADMIN")
                .build();

        // Return the user details manager with the two users
        return new InMemoryUserDetailsManager(adminUser, superUser);
    }
}
