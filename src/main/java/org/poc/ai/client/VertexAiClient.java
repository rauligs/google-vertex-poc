package org.poc.ai.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.poc.ai.auth.GoogleAuthService;
import org.poc.ai.config.EnvironmentConfig;
import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with Google Vertex AI API to access Gemini models
 */
public class VertexAiClient {
    private static final Logger logger = LoggerFactory.getLogger(VertexAiClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    
    private final OkHttpClient httpClient;
    private final GoogleAuthService authService;
    private final String projectId;
    private final String location;
    private final String publisher;
    private final String modelId;
    private final String vertexApiEndpoint;
    
    public VertexAiClient(GoogleAuthService authService) {
        this.authService = authService;
        this.projectId = EnvironmentConfig.get("PROJECT_ID");
        this.location = EnvironmentConfig.get("LOCATION", "us-central1");
        this.publisher = EnvironmentConfig.get("PUBLISHER", "google");
        this.modelId = EnvironmentConfig.get("MODEL_ID", "gemini-flash");
        this.vertexApiEndpoint = EnvironmentConfig.get("VERTEX_API_ENDPOINT", 
                "https://us-central1-aiplatform.googleapis.com");
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        logger.info("Initialized Vertex AI client for model: {}/{}", publisher, modelId);
    }
    
    /**
     * Constructor with custom HTTP client for testing
     */
    public VertexAiClient(GoogleAuthService authService, OkHttpClient httpClient, 
                          String projectId, String location, String publisher, 
                          String modelId, String vertexApiEndpoint) {
        this.authService = authService;
        this.httpClient = httpClient;
        this.projectId = projectId;
        this.location = location;
        this.publisher = publisher;
        this.modelId = modelId;
        this.vertexApiEndpoint = vertexApiEndpoint;
    }
    
    /**
     * Generates content using the Gemini Flash model
     * 
     * @param request the request containing the prompt and other parameters
     * @return the model's response
     * @throws IOException if there's an error with the API call
     */
    public GeminiResponse generateContent(GeminiRequest request) throws IOException {
        String accessToken = authService.getAccessToken();
        String url = buildGenerateContentUrl();
        
        RequestBody requestBody = RequestBody.create(
                gson.toJson(request), 
                JSON
        );
        
        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();
        
        logger.debug("Sending request to Vertex AI: {}", url);
        
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                logger.error("API call failed with code {}: {}", response.code(), errorBody);
                throw new IOException("API call failed with code " + response.code() + ": " + errorBody);
            }
            
            String responseBody = response.body().string();
            logger.debug("Received response from Vertex AI");
            
            return gson.fromJson(responseBody, GeminiResponse.class);
        }
    }
    
    /**
     * Builds the URL for the generateContent endpoint
     * 
     * @return the complete URL for the API call
     */
    private String buildGenerateContentUrl() {
        return String.format("%s/v1/projects/%s/locations/%s/publishers/%s/models/%s:generateContent",
                vertexApiEndpoint, projectId, location, publisher, modelId);
    }
}
