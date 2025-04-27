package org.poc.ai.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a request to the Gemini model
 */
public class GeminiRequest {
    private List<Content> contents;
    private GenerationConfig generationConfig;
    private SafetySetting[] safetySettings;
    
    public GeminiRequest() {
        this.contents = new ArrayList<>();
    }
    
    public static GeminiRequest createTextRequest(String text) {
        GeminiRequest request = new GeminiRequest();
        Content content = new Content();
        
        Part textPart = new Part();
        textPart.setText(text);
        
        content.setParts(List.of(textPart));
        content.setRole("user");
        request.setContents(List.of(content));
        
        return request;
    }
    
    public List<Content> getContents() {
        return contents;
    }
    
    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
    
    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }
    
    public void setGenerationConfig(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }
    
    public SafetySetting[] getSafetySettings() {
        return safetySettings;
    }
    
    public void setSafetySettings(SafetySetting[] safetySettings) {
        this.safetySettings = safetySettings;
    }
    
    /**
     * Represents content in a Gemini request
     */
    public static class Content {
        private List<Part> parts;
        private String role;
        
        public List<Part> getParts() {
            return parts;
        }
        
        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
        
        public String getRole() {
            return role;
        }
        
        public void setRole(String role) {
            this.role = role;
        }
    }
    
    /**
     * Represents a part of content in a Gemini request
     */
    public static class Part {
        private String text;
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
    }
    
    /**
     * Configuration for text generation
     */
    public static class GenerationConfig {
        private Double temperature;
        private Integer maxOutputTokens;
        private Double topP;
        private Integer topK;
        
        public Double getTemperature() {
            return temperature;
        }
        
        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
        
        public Integer getMaxOutputTokens() {
            return maxOutputTokens;
        }
        
        public void setMaxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
        }
        
        public Double getTopP() {
            return topP;
        }
        
        public void setTopP(Double topP) {
            this.topP = topP;
        }
        
        public Integer getTopK() {
            return topK;
        }
        
        public void setTopK(Integer topK) {
            this.topK = topK;
        }
    }
    
    /**
     * Safety settings for content generation
     */
    public static class SafetySetting {
        private String category;
        private String threshold;
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getThreshold() {
            return threshold;
        }
        
        public void setThreshold(String threshold) {
            this.threshold = threshold;
        }
    }
}
