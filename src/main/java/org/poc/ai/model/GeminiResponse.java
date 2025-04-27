package org.poc.ai.model;

import java.util.List;

/**
 * Represents a response from the Gemini model
 */
public class GeminiResponse {
    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;
    
    public List<Candidate> getCandidates() {
        return candidates;
    }
    
    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
    
    public PromptFeedback getPromptFeedback() {
        return promptFeedback;
    }
    
    public void setPromptFeedback(PromptFeedback promptFeedback) {
        this.promptFeedback = promptFeedback;
    }
    
    /**
     * Gets the text content from the first candidate's first part
     * 
     * @return the generated text or null if no content is available
     */
    public String getGeneratedText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate candidate = candidates.get(0);
            if (candidate.getContent() != null && 
                candidate.getContent().getParts() != null && 
                !candidate.getContent().getParts().isEmpty()) {
                return candidate.getContent().getParts().get(0).getText();
            }
        }
        return null;
    }
    
    /**
     * Represents a candidate response from the model
     */
    public static class Candidate {
        private GeminiRequest.Content content;
        private String finishReason;
        private int index;
        private List<SafetyRating> safetyRatings;
        
        public GeminiRequest.Content getContent() {
            return content;
        }
        
        public void setContent(GeminiRequest.Content content) {
            this.content = content;
        }
        
        public String getFinishReason() {
            return finishReason;
        }
        
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
        
        public int getIndex() {
            return index;
        }
        
        public void setIndex(int index) {
            this.index = index;
        }
        
        public List<SafetyRating> getSafetyRatings() {
            return safetyRatings;
        }
        
        public void setSafetyRatings(List<SafetyRating> safetyRatings) {
            this.safetyRatings = safetyRatings;
        }
    }
    
    /**
     * Represents safety ratings for generated content
     */
    public static class SafetyRating {
        private String category;
        private String probability;
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getProbability() {
            return probability;
        }
        
        public void setProbability(String probability) {
            this.probability = probability;
        }
    }
    
    /**
     * Represents feedback on the prompt
     */
    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;
        
        public List<SafetyRating> getSafetyRatings() {
            return safetyRatings;
        }
        
        public void setSafetyRatings(List<SafetyRating> safetyRatings) {
            this.safetyRatings = safetyRatings;
        }
    }
}
