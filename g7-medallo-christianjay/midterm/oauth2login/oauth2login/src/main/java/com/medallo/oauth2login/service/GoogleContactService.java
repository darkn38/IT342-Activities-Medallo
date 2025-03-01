package com.medallo.oauth2login.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.people.v1.model.*;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleContactService {

    private static final String GOOGLE_PEOPLE_API_URL = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses,phoneNumbers";
    private static final String GOOGLE_CREATE_CONTACT_URL = "https://people.googleapis.com/v1/people:createContact";
    private static final String GOOGLE_UPDATE_CONTACT_URL = "https://people.googleapis.com/v1/{resourceName}:updateContact";
    private static final String GOOGLE_DELETE_CONTACT_URL = "https://people.googleapis.com/v1/{resourceName}";

    private static String syncToken = null; // Stores sync token for incremental updates

    private final OAuth2AuthorizedClientService authorizedClientService;

    public GoogleContactService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * Fetches all user contacts (full sync).
     */
    public List<Person> getUserContacts(OAuth2AuthenticationToken authentication) throws IOException {
        String accessToken = getAccessToken(authentication);
        if (accessToken == null) {
            throw new RuntimeException("No valid access token found.");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = GOOGLE_PEOPLE_API_URL + "&access_token=" + accessToken;

        // Fetch raw JSON response
        String jsonResponse = restTemplate.getForObject(url, String.class);

        // Parse JSON manually using Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode connectionsNode = rootNode.path("connections");

        List<Person> contacts = new ArrayList<>();
        if (connectionsNode.isArray()) {
            for (JsonNode contactNode : connectionsNode) {
                Person person = objectMapper.treeToValue(contactNode, Person.class);
                contacts.add(person);
            }
        }

        return contacts;
    }

    /**
     * Fetches only changed contacts since last sync (incremental sync).
     */
    public List<Person> getChangedContacts(OAuth2AuthenticationToken authentication) throws IOException {
        if (syncToken == null) {
            return getUserContacts(authentication); // If no sync token, do a full sync
        }

        String accessToken = getAccessToken(authentication);
        if (accessToken == null) {
            throw new RuntimeException("No valid access token found.");
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = GOOGLE_PEOPLE_API_URL + "&syncToken=" + syncToken;

        ListConnectionsResponse response = restTemplate.getForObject(url + "&access_token=" + accessToken, ListConnectionsResponse.class);

        if (response == null || response.getConnections() == null) {
            return new ArrayList<>();
        }

        // Update sync token
        syncToken = response.getNextSyncToken();

        return response.getConnections();
    }

    /**
     * Creates a new contact in Google Contacts.
     */
    public void createContact(String name, String email, String phoneNumber, OAuth2AuthenticationToken authentication) {
        try {
            String accessToken = getAccessToken(authentication);
            if (accessToken == null) {
                throw new RuntimeException("No valid access token found.");
            }

            // Construct the new contact
            Person newContact = new Person();
            newContact.setNames(Collections.singletonList(new Name().setGivenName(name)));
            newContact.setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(email)));
            newContact.setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(phoneNumber)));

            // Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Person> request = new HttpEntity<>(newContact, headers);
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.postForObject(GOOGLE_CREATE_CONTACT_URL, request, Person.class);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error creating contact: " + e.getMessage());
        }
    }

    /**
     * Updates an existing contact in Google Contacts.
     */
    public void updateContact(String resourceName, String name, String email, String phoneNumber, OAuth2AuthenticationToken authentication) {
        try {
            String accessToken = getAccessToken(authentication);
            if (accessToken == null) {
                throw new RuntimeException("No valid access token found.");
            }

            // Construct the updated contact
            Person updatedContact = new Person();
            updatedContact.setNames(Collections.singletonList(new Name().setGivenName(name)));
            updatedContact.setEmailAddresses(Collections.singletonList(new EmailAddress().setValue(email)));
            updatedContact.setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(phoneNumber)));

            // Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Person> request = new HttpEntity<>(updatedContact, headers);
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.exchange(
                    GOOGLE_UPDATE_CONTACT_URL.replace("{resourceName}", resourceName) + "?updatePersonFields=names,emailAddresses,phoneNumbers",
                    HttpMethod.PATCH,
                    request,
                    Person.class
            );

        } catch (Exception e) {
            throw new RuntimeException("❌ Error updating contact: " + e.getMessage());
        }
    }

    /**
     * Deletes a contact from Google Contacts.
     */
    public void deleteContact(String resourceName, OAuth2AuthenticationToken authentication) {
        try {
            String accessToken = getAccessToken(authentication);
            if (accessToken == null) {
                throw new RuntimeException("No valid access token found.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();

            restTemplate.exchange(GOOGLE_DELETE_CONTACT_URL.replace("{resourceName}", resourceName), HttpMethod.DELETE, request, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("❌ Error deleting contact: " + e.getMessage());
        }
    }

    private String getAccessToken(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        return client != null ? client.getAccessToken().getTokenValue() : null;
    }
}
