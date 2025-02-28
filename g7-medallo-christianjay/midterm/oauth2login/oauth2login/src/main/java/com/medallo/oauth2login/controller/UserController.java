package com.medallo.oauth2login.controller;

import com.google.api.services.people.v1.model.Person;
import com.medallo.oauth2login.service.GoogleContactService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api1")
public class UserController {

    private final GoogleContactService googleContactService;

    public UserController(GoogleContactService googleContactService) {
        this.googleContactService = googleContactService;
    }

    @GetMapping
    public String index() {
        return "<h1> Welcome, this is the landing page of our home city!</h1>";
    }

    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User != null)
            return oAuth2User.getAttributes();
        else
            return Collections.emptyMap();
    }

    @GetMapping("/contacts")
    public List<Person> getUserContacts() throws IOException {
        return googleContactService.getUserContacts();
    }
}
