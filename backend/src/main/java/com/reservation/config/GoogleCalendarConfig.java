package com.reservation.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {
    
    private static final String APPLICATION_NAME = "University Reservation System";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    @Value("${google.calendar.credentials.json:}")
    private String credentialsJson;
    
    @Value("${google.calendar.enabled:false}")
    private boolean googleCalendarEnabled;
    
    @Bean
    public Calendar googleCalendar() throws GeneralSecurityException, IOException {
        if (!googleCalendarEnabled || credentialsJson.isEmpty()) {
            return null; // Calendar integration disabled
        }
        
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        // Create credentials from JSON string
        GoogleCredential credential = GoogleCredential
            .fromStream(new ByteArrayInputStream(credentialsJson.getBytes()))
            .createScoped(Collections.singleton(CalendarScopes.CALENDAR));
        
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    
    public boolean isGoogleCalendarEnabled() {
        return googleCalendarEnabled;
    }
}
