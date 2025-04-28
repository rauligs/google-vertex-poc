package org.poc.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.poc.ai.client.VertexAiClient;
import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for GeminiRestController with focus on validation
 */
@WebMvcTest(GeminiRestController.class)
class GeminiRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VertexAiClient vertexAiClient;

    @Test
    @DisplayName("Valid request should return 200 OK")
    void validRequestShouldReturnOk() throws Exception {
        // Create a valid request
        GeminiRequest request = GeminiRequest.createTextRequest("Test prompt");

        // Mock the client response
        GeminiResponse mockResponse = new GeminiResponse();
        when(vertexAiClient.generateContent(any(GeminiRequest.class))).thenReturn(mockResponse);

        // Perform the request
        mockMvc.perform(post("/api/gemini/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Request with null contents should return 400 Bad Request")
    void requestWithNullContentsShouldReturnBadRequest() throws Exception {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        request.setContents(null);

        // Perform the request
        mockMvc.perform(post("/api/gemini/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Contents cannot be empty")));
    }

    @Test
    @DisplayName("Request with empty contents should return 400 Bad Request")
    void requestWithEmptyContentsShouldReturnBadRequest() throws Exception {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        request.setContents(new ArrayList<>());

        // Perform the request
        mockMvc.perform(post("/api/gemini/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Contents cannot be empty")));
    }

    @Test
    @DisplayName("Content with null parts should return 400 Bad Request")
    void contentWithNullPartsShouldReturnBadRequest() throws Exception {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        content.setParts(null);
        content.setRole("user");
        request.setContents(List.of(content));

        // Perform the request
        mockMvc.perform(post("/api/gemini/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Parts cannot be empty")));
    }

    @Test
    @DisplayName("Content with empty parts should return 400 Bad Request")
    void contentWithEmptyPartsShouldReturnBadRequest() throws Exception {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        content.setParts(new ArrayList<>());
        content.setRole("user");
        request.setContents(List.of(content));

        // Perform the request
        mockMvc.perform(post("/api/gemini/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Parts cannot be empty")));
    }

    @Test
    @DisplayName("Part with null text should return 400 Bad Request")
    void partWithNullTextShouldReturnBadRequest() throws Exception {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        GeminiRequest.Part part = new GeminiRequest.Part();
        part.setText(null);
        content.setParts(List.of(part));
        content.setRole("user");
        request.setContents(List.of(content));

        // Perform the request
        mockMvc.perform(post("/api/gemini/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.containsString("Text cannot be null")));
    }

    @Test
    @DisplayName("Multiple validation errors should return 400 Bad Request with all errors")
    void multipleValidationErrorsShouldReturnBadRequestWithAllErrors() throws Exception {
        // Create an invalid request with multiple issues
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        GeminiRequest.Part part = new GeminiRequest.Part();
        part.setText(null);
        content.setParts(List.of(part));
        
        GeminiRequest.Content emptyContent = new GeminiRequest.Content();
        emptyContent.setParts(new ArrayList<>());
        
        request.setContents(List.of(content, emptyContent));

        // Perform the request
        mockMvc.perform(post("/api/gemini/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("Text cannot be null"),
                        org.hamcrest.Matchers.containsString("Parts cannot be empty")
                )));
    }
}
