package com.medallo.oauth2login.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleContactService {

    private static final String APPLICATION_NAME = "YourAppName";

    public GoogleContactService() throws GeneralSecurityException, IOException {

    }

    public List<Person> getUserContacts() throws IOException {
        List<Person> contacts = new ArrayList<>();

        return contacts;
    }
}
