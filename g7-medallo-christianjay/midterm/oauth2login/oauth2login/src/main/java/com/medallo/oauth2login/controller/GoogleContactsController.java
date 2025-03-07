package com.medallo.oauth2login.controller;

import com.medallo.oauth2login.service.GoogleContactService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/google")
public class GoogleContactsController {

    private final GoogleContactService googleContactService;

    public GoogleContactsController(GoogleContactService googleContactService) {
        this.googleContactService = googleContactService;
    }
}
