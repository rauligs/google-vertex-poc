package org.poc.ai.service;

import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;
import org.poc.ai.model.springai.SpringAiGeminiRequest;
import org.poc.ai.model.springai.SpringAiGeminiResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts between Spring AI Gemini models and Vertex AI Gemini models
 */
public class GeminiModelConverter {

    /**
     * Converts a Spring AI Gemini request to a Vertex AI Gemini request
     *
     * @param springRequest the Spring AI request to convert
     * @return the equivalent Vertex AI request
     */
    public GeminiRequest convertToVertexRequest(SpringAiGeminiRequest springRequest) {
        GeminiRequest vertexRequest = new GeminiRequest();
        List<GeminiRequest.Content> contents = new ArrayList<>();

        // Convert messages
        if (springRequest.getMessages() != null) {
            for (SpringAiGeminiRequest.Message springMessage : springRequest.getMessages()) {
                GeminiRequest.Content content = new GeminiRequest.Content();
                content.setRole(springMessage.getRole());

                GeminiRequest.Part part = new GeminiRequest.Part();
                // Use content if available, otherwise try to extract from parts
                if (springMessage.getContent() != null) {
                    part.setText(springMessage.getContent());
                } else if (springMessage.getParts() != null && !springMessage.getParts().isEmpty()) {
                    // Assuming the first part with a "text" key is the main content
                    for (Map<String, String> partMap : springMessage.getParts()) {
                        if (partMap.containsKey("text")) {
                            part.setText(partMap.get("text"));
                            break;
                        }
                    }
                }

                content.setParts(List.of(part));
                contents.add(content);
            }
        }

        vertexRequest.setContents(contents);

        // Convert generation options if present
        if (springRequest.getOptions() != null) {
            GeminiRequest.GenerationConfig generationConfig = new GeminiRequest.GenerationConfig();
            Map<String, Object> options = springRequest.getOptions();

            if (options.containsKey("temperature")) {
                generationConfig.setTemperature(parseDoubleOption(options, "temperature"));
            }
            if (options.containsKey("maxOutputTokens") || options.containsKey("max_tokens")) {
                Integer maxTokens = parseIntOption(options, "maxOutputTokens");
                if (maxTokens == null) {
                    maxTokens = parseIntOption(options, "max_tokens");
                }
                generationConfig.setMaxOutputTokens(maxTokens);
            }
            if (options.containsKey("topP")) {
                generationConfig.setTopP(parseDoubleOption(options, "topP"));
            }
            if (options.containsKey("topK")) {
                generationConfig.setTopK(parseIntOption(options, "topK"));
            }

            vertexRequest.setGenerationConfig(generationConfig);
        }

        return vertexRequest;
    }

    /**
     * Converts a Vertex AI Gemini response to a Spring AI Gemini response
     *
     * @param vertexResponse the Vertex AI response to convert
     * @param modelId the model ID to include in the response
     * @return the equivalent Spring AI response
     */
    public SpringAiGeminiResponse convertToSpringResponse(GeminiResponse vertexResponse, String modelId) {
        SpringAiGeminiResponse springResponse = new SpringAiGeminiResponse();
        
        // Set metadata
        springResponse.setId(UUID.randomUUID().toString());
        springResponse.setObject("chat.completion");
        springResponse.setCreated(Instant.now().getEpochSecond());
        springResponse.setModel(modelId);
        
        // Convert candidates to choices
        if (vertexResponse.getCandidates() != null) {
            List<SpringAiGeminiResponse.Choice> choices = vertexResponse.getCandidates().stream()
                .map(candidate -> {
                    SpringAiGeminiResponse.Choice choice = new SpringAiGeminiResponse.Choice();
                    choice.setIndex(candidate.getIndex());
                    choice.setFinishReason(candidate.getFinishReason());
                    
                    // Create message from content
                    if (candidate.getContent() != null && 
                        candidate.getContent().getParts() != null && 
                        !candidate.getContent().getParts().isEmpty()) {
                        
                        SpringAiGeminiResponse.Message message = new SpringAiGeminiResponse.Message();
                        message.setRole("assistant");
                        message.setContent(candidate.getContent().getParts().get(0).getText());
                        choice.setMessage(message);
                    }
                    
                    return choice;
                })
                .collect(Collectors.toList());
            
            springResponse.setChoices(choices);
        }
        
        // Add token usage (estimated since Vertex AI doesn't provide this)
        SpringAiGeminiResponse.Usage usage = new SpringAiGeminiResponse.Usage();
        // These are placeholder values since Vertex AI doesn't provide token counts
        usage.setPromptTokens(0);
        usage.setCompletionTokens(0);
        usage.setTotalTokens(0);
        springResponse.setUsage(usage);
        
        return springResponse;
    }
    
    private Double parseDoubleOption(Map<String, Object> options, String key) {
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private Integer parseIntOption(Map<String, Object> options, String key) {
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
