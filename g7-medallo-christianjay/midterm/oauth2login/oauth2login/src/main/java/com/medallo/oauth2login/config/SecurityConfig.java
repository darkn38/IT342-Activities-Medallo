package com.medallo.oauth2login.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/contacts").authenticated()  // Protect Google Contacts API
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/user-info", true)
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new OidcUserService())) // Handle OAuth2 user info
                )
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .formLogin(formLogin -> formLogin.defaultSuccessUrl("/user-info", true))
                .build();
    }
}
