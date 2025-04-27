package org.poc.ai.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.poc.ai.auth.GoogleAuthService;
import org.poc.ai.client.VertexAiClient;
import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that demonstrates mocking both Google authentication and Vertex AI calls
 */
public class GeminiClientIntegrationTest {
    
    private WireMockServer wireMockServer;
    private Path credentialsFile;
    private VertexAiClient vertexAiClient;
    private TestGoogleAuthService authService;
    private final Gson gson = new Gson();
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    public void setup() throws IOException {
        // Start WireMock server
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        
        // Create a mock service account key file
        credentialsFile = tempDir.resolve("mock-credentials.json");
        String mockCredentials = "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"test-project\",\n" +
                "  \"private_key_id\": \"mock-key-id\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC7VJTUt9Us8cKj\\nMzEfYyjiWA4R4/M2bS1GB4t7NXp98C3SC6dVMvDuictGeurT8jNbvJZHtCSuYEvu\\nNMoSfm76oqFvAp8Gy0iz5sxjZmSnXyCdPEovGhLa0VzMaQ8s+CLOyS56YyCFGeJZ\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"test-account@test-project.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"123456789\",\n" +
                "  \"auth_uri\": \"" + wireMockServer.baseUrl() + "/auth\",\n" +
                "  \"token_uri\": \"" + wireMockServer.baseUrl() + "/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"" + wireMockServer.baseUrl() + "/certs\",\n" +
                "  \"client_x509_cert_url\": \"" + wireMockServer.baseUrl() + "/x509/test-account@test-project.iam.gserviceaccount.com\"\n" +
                "}";
        Files.writeString(credentialsFile, mockCredentials);
        
        // Setup mock for Google Auth
        setupGoogleAuthMock();
        
        // Create auth service with the mock credentials file
        authService = new TestGoogleAuthService(credentialsFile.toString(), wireMockServer.baseUrl());
        
        // Create client with our test configuration
        vertexAiClient = new VertexAiClient(
                authService,
                new OkHttpClient(),
                "test-project",
                "test-location",
                "google",
                "gemini-flash",
                wireMockServer.baseUrl()
        );
    }
    
    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }
    
    @Test
    public void testEndToEndGeminiFlashIntegration() throws IOException {
        // Setup mock for Vertex AI endpoint
        setupVertexAiMock("Tell me a joke about programming", 
                "Why do programmers prefer dark mode? Because light attracts bugs!");
        
        // Create request
        GeminiRequest request = GeminiRequest.createTextRequest("Tell me a joke about programming");
        
        // Set generation parameters
        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig();
        config.setTemperature(0.7);
        config.setMaxOutputTokens(100);
        request.setGenerationConfig(config);
        
        // Call the Gemini model
        GeminiResponse response = vertexAiClient.generateContent(request);
        
        // Verify the response
        assertNotNull(response);
        assertEquals("Why do programmers prefer dark mode? Because light attracts bugs!", 
                response.getGeneratedText());
        
        // Verify both mocks were called
        verify(postRequestedFor(urlEqualTo("/token")));
        verify(postRequestedFor(urlPathMatching(".*/gemini-flash:generateContent")));
    }
    
    /**
     * Sets up the mock for Google authentication
     */
    private void setupGoogleAuthMock() {
        stubFor(post(urlEqualTo("/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"access_token\":\"mock-access-token\",\"expires_in\":3600,\"token_type\":\"Bearer\"}")));
    }
    
    /**
     * Sets up the mock for Vertex AI endpoint
     */
    private void setupVertexAiMock(String expectedPrompt, String responseText) {
        // Create mock response
        GeminiResponse mockResponse = createMockResponse(responseText);
        String mockResponseJson = gson.toJson(mockResponse);
        
        // Setup WireMock stub for the Vertex AI endpoint
        stubFor(post(urlPathMatching("/v1/projects/test-project/locations/test-location/publishers/google/models/gemini-flash:generateContent"))
                .withHeader("Authorization", equalTo("Bearer mock-access-token"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseJson)));
    }
    
    /**
     * Helper method to create a mock GeminiResponse
     */
    private GeminiResponse createMockResponse(String text) {
        GeminiResponse response = new GeminiResponse();
        
        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
        GeminiRequest.Content content = new GeminiRequest.Content();
        GeminiRequest.Part part = new GeminiRequest.Part();
        
        part.setText(text);
        content.setParts(java.util.List.of(part));
        candidate.setContent(content);
        candidate.setFinishReason("STOP");
        candidate.setIndex(0);
        
        response.setCandidates(java.util.List.of(candidate));
        
        return response;
    }
    
    /**
     * Test implementation of GoogleAuthService that uses WireMock
     */
    private class TestGoogleAuthService extends GoogleAuthService {
        
        private final String tokenEndpoint;
        
        public TestGoogleAuthService(String credentialsPath, String tokenEndpoint) {
            super(credentialsPath);
            this.tokenEndpoint = tokenEndpoint;
        }
        
        @Override
        public String getAccessToken() throws IOException {
            // For testing, we'll simulate the token request to the WireMock server
            try {
                // Make a POST request to the token endpoint
                okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
                okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
                okhttp3.RequestBody body = okhttp3.RequestBody.create("grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=test-assertion", mediaType);
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(tokenEndpoint + "/token")
                        .post(body)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();
                
                okhttp3.Response response = client.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    throw new IOException("Error getting access token: " + response.code() + " " + 
                            (response.body() != null ? response.body().string() : ""));
                }
                
                return "mock-access-token";
            } catch (Exception e) {
                throw new IOException("Error getting access token: " + e.getMessage(), e);
            }
        }
    }
}
