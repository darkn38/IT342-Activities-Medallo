package com.medallo.oauth2login.controller;

import com.google.api.services.people.v1.model.Person;
import com.medallo.oauth2login.service.GoogleContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/google")
public class GoogleContactsController {

    private final GoogleContactService googleContactService;

    public GoogleContactsController(GoogleContactService googleContactService) {
        this.googleContactService = googleContactService;
    }

    @GetMapping("/contacts")
    public List<Person> getGoogleContacts(OAuth2AuthenticationToken authentication) throws IOException {
        return googleContactService.getUserContacts(authentication);
    }

    @GetMapping("/changed-contacts")
    public List<Person> getChangedGoogleContacts(OAuth2AuthenticationToken authentication) throws IOException {
        return googleContactService.getChangedContacts(authentication);
    }
    /**
     * Add a new contact to Google Contacts
     */
    @PostMapping("/contacts/add")
    public ResponseEntity<?> addContact(@RequestBody Map<String, String> requestBody, OAuth2AuthenticationToken authentication) {
        try {
            String name = requestBody.get("name");
            String email = requestBody.get("email");
            String phoneNumber = requestBody.get("phoneNumber");

            googleContactService.createContact(name, email, phoneNumber, authentication);
            return ResponseEntity.ok("✅ Contact added successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error adding contact: " + e.getMessage());
        }
    }

    /**
     * Edit an existing contact
     */
    @PutMapping("/contacts/edit/{contactId}")
    public ResponseEntity<?> editContact(@PathVariable String contactId, @RequestBody Map<String, String> requestBody, OAuth2AuthenticationToken authentication) {
        try {
            String name = requestBody.get("name");
            String email = requestBody.get("email");
            String phoneNumber = requestBody.get("phoneNumber");

            googleContactService.updateContact(contactId, name, email, phoneNumber, authentication);
            return ResponseEntity.ok("✅ Contact updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error updating contact: " + e.getMessage());
        }
    }

    /**
     * Delete a contact from Google Contacts
     */
    @DeleteMapping("/contacts/delete/{contactId}")
    public ResponseEntity<?> deleteContact(@PathVariable String contactId, OAuth2AuthenticationToken authentication) {
        try {
            googleContactService.deleteContact(contactId, authentication);
            return ResponseEntity.ok("✅ Contact deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error deleting contact: " + e.getMessage());
        }
    }
}
