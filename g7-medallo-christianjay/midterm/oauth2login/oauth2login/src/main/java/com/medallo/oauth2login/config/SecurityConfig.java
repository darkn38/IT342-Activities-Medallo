package com.medallo.oauth2login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/contacts").authenticated()  // Protects fetching contacts
                        .requestMatchers("/api/contacts/edit/**").authenticated() // Protects editing contacts
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable()) // Disable CSRF if using REST API calls from frontend
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/api/user-info", true)  // Redirect to /api/user-info after login
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new OidcUserService())) // Handle OAuth2 user info
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login")  // Redirect to login page after logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID") // Optional: Remove session cookie
                )
                .formLogin(formLogin -> formLogin.defaultSuccessUrl("/api/user-info", true)) // Ensure form login also redirects correctly
                .build();
    }
}
