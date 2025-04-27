package org.poc.ai.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.poc.ai.auth.GoogleAuthService;
import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the VertexAiClient using WireMock to mock external services
 */
public class VertexAiClientTest {
    
    private WireMockServer wireMockServer;
    private GoogleAuthService mockAuthService;
    private VertexAiClient vertexAiClient;
    private final Gson gson = new Gson();
    
    @BeforeEach
    public void setup() throws IOException {
        // Start WireMock server
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        
        // Create mock auth service
        mockAuthService = mock(GoogleAuthService.class);
        when(mockAuthService.getAccessToken()).thenReturn("mock-access-token");
        
        // Create client with mocked dependencies
        vertexAiClient = new VertexAiClient(
                mockAuthService,
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
    public void testGenerateContent_Success() throws IOException {
        // Prepare mock response
        GeminiResponse mockResponse = createMockResponse("This is a mock response from Gemini Flash.");
        String mockResponseJson = gson.toJson(mockResponse);
        
        // Setup WireMock stub for the Vertex AI endpoint
        stubFor(post(urlPathMatching("/v1/projects/test-project/locations/test-location/publishers/google/models/gemini-flash:generateContent"))
                .withHeader("Authorization", equalTo("Bearer mock-access-token"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponseJson)));
        
        // Create request
        GeminiRequest request = GeminiRequest.createTextRequest("Test prompt");
        
        // Call the method under test
        GeminiResponse response = vertexAiClient.generateContent(request);
        
        // Verify the response
        assertNotNull(response);
        assertEquals("This is a mock response from Gemini Flash.", response.getGeneratedText());
        
        // Verify the auth service was called
        verify(mockAuthService, times(1)).getAccessToken();
        
        // Verify the request to Vertex AI was made correctly
        verify(postRequestedFor(urlPathMatching("/v1/projects/test-project/locations/test-location/publishers/google/models/gemini-flash:generateContent"))
                .withHeader("Authorization", equalTo("Bearer mock-access-token"))
                .withHeader("Content-Type", containing("application/json")));
    }
    
    @Test
    public void testGenerateContent_ApiError() throws IOException {
        // Setup WireMock stub for error response
        stubFor(post(urlPathMatching("/v1/projects/test-project/locations/test-location/publishers/google/models/gemini-flash:generateContent"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"error\": {\"code\": 400, \"message\": \"Invalid request\"}}")));
        
        // Create request
        GeminiRequest request = GeminiRequest.createTextRequest("Test prompt");
        
        // Call the method and expect exception
        IOException exception = assertThrows(IOException.class, () -> {
            vertexAiClient.generateContent(request);
        });
        
        // Verify exception message contains error details
        assertTrue(exception.getMessage().contains("400"));
        assertTrue(exception.getMessage().contains("Invalid request"));
        
        // Verify the auth service was called
        verify(mockAuthService, times(1)).getAccessToken();
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
}
