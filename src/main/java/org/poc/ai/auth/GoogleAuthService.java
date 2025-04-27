package org.poc.ai.auth;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.poc.ai.config.EnvironmentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

/**
 * Service for handling Google authentication using service accounts
 */
public class GoogleAuthService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthService.class);
    private static final String VERTEX_AI_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    
    private final String credentialsPath;
    
    public GoogleAuthService() {
        this.credentialsPath = EnvironmentConfig.get("GOOGLE_APPLICATION_CREDENTIALS");
        if (this.credentialsPath == null) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS environment variable is not set");
        }
    }
    
    public GoogleAuthService(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }
    
    /**
     * Gets an access token for Google API authentication
     * 
     * @return the access token
     * @throws IOException if there's an error reading credentials or getting the token
     */
    public String getAccessToken() throws IOException {
        logger.debug("Getting access token using credentials from: {}", credentialsPath);
        
        GoogleCredentials credentials = ServiceAccountCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(Collections.singleton(VERTEX_AI_SCOPE));
        
        // Force token refresh
        credentials.refreshIfExpired();
        return credentials.getAccessToken().getTokenValue();
    }
}
