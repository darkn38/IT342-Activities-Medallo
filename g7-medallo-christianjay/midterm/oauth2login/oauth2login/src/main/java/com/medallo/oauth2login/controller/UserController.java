package com.medallo.oauth2login.controller;

import com.google.api.services.people.v1.model.Person;
import com.medallo.oauth2login.service.GoogleContactService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller  // 🔹 Changed from @RestController to @Controller for Thymeleaf support
@RequestMapping("/api")
public class UserController {

    private final GoogleContactService googleContactService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserController(GoogleContactService googleContactService, OAuth2AuthorizedClientService authorizedClientService) {
        this.googleContactService = googleContactService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping
    public String index() {
        return "<h1> Welcome, this is the landing page of our home city!</h1>";
    }

    @GetMapping(value = "/user-info", produces = "text/html")
    @ResponseBody // ✅ Add this annotation to prevent Thymeleaf from treating it as a template name
    public String getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        try {
            if (oAuth2User == null) {
                return "<h3 style='color:red;'>❌ User not authenticated.</h3>";
            }

            Map<String, Object> userInfo = oAuth2User.getAttributes();
            if (userInfo == null || userInfo.isEmpty()) {
                return "<h3 style='color:red;'>❌ No user info found.</h3>";
            }

            // Build response
            StringBuilder response = new StringBuilder("<h2>👤 User Information</h2><pre>");
            response.append("{\n");
            userInfo.forEach((key, value) -> response.append("  \"").append(key).append("\": \"").append(value).append("\",\n"));
            response.deleteCharAt(response.length() - 2);
            response.append("}\n</pre>");

            response.append("<br><a href='/api/contacts'>📇 View Contacts</a>");
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "<h3 style='color:red;'>❌ Error fetching user info: " + e.getMessage() + "</h3>";
        }
    }



    @GetMapping("/contacts")
    public String getUserContacts(OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            model.addAttribute("error", "❌ No authentication token found.");
            return "contacts"; // Returns contacts.html with an error message
        }

        try {
            List<Person> contacts = googleContactService.getUserContacts(authentication);

            // 🔍 Debug: Print full contact data
            for (Person contact : contacts) {
                System.out.println("✅ Contact: " + contact);
            }

            model.addAttribute("contacts", contacts);
            return "contacts"; // Render the Thymeleaf page

        } catch (Exception e) {
            model.addAttribute("error", "❌ Error fetching contacts: " + e.getMessage());
            return "contacts"; // Render the page with the error message
        }
    }




    @GetMapping("/access-token")
    public String getAccessToken(OAuth2AuthenticationToken authentication) {
        if (authentication == null) {
            return "❌ No authentication found!";
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService
                .loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            return "Access Token: " + authorizedClient.getAccessToken().getTokenValue();
        }
        return "❌ No access token found!";
    }
}
