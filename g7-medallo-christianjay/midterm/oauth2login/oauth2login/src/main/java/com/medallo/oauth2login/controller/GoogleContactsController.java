package com.medallo.oauth2login.controller;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class GoogleContactsController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactsController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/contacts")
    public String getGoogleContacts(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();
        String contactsApiUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(
                contactsApiUrl + "&access_token=" + accessToken, String.class
        );

        return response;
    }
}
