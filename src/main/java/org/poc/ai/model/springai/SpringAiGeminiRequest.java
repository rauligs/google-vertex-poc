package org.poc.ai.model.springai;

import java.util.List;
import java.util.Map;

/**
 * Represents a Spring AI Gemini request model.
 * This is a simplified version based on Spring AI's structure.
 */
public class SpringAiGeminiRequest {
    private List<Message> messages;
    private Map<String, Object> options;

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    /**
     * Represents a message in the Spring AI Gemini request
     */
    public static class Message {
        private String role;
        private String content;
        private List<Map<String, String>> parts;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<Map<String, String>> getParts() {
            return parts;
        }

        public void setParts(List<Map<String, String>> parts) {
            this.parts = parts;
        }
    }
}
