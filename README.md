# Google Vertex AI Gemini Flash Client

A Java client for calling the Gemini Flash model via Google Vertex AI, with support for integration testing using WireMock.

## Features

- Call the Gemini Flash model via Google Vertex AI
- Authentication with Google service account
- Environment variable configuration via .env file
- Comprehensive integration testing with WireMock
- Mock both Google authentication and Vertex AI calls for testing

## Prerequisites

- Java 11 or higher
- Gradle
- Google Cloud account with Vertex AI enabled
- Service account with appropriate permissions

## Setup

1. Clone the repository
2. Create a `.env` file in the project root based on the `.env.example` template:

```
# Google Cloud Configuration
GOOGLE_APPLICATION_CREDENTIALS=path/to/service-account-key.json
PROJECT_ID=your-google-cloud-project-id
LOCATION=us-central1
PUBLISHER=google
MODEL_ID=gemini-flash

# Vertex AI API Configuration
VERTEX_API_ENDPOINT=https://us-central1-aiplatform.googleapis.com
```

3. Place your Google service account key file in a secure location and update the `GOOGLE_APPLICATION_CREDENTIALS` path in the `.env` file.

## Usage

The main class demonstrates how to use the client:

```java
// Initialize authentication service
GoogleAuthService authService = new GoogleAuthService();

// Initialize Vertex AI client
VertexAiClient vertexAiClient = new VertexAiClient(authService);

// Create a simple text request
GeminiRequest request = GeminiRequest.createTextRequest("Tell me a short joke about programming");

// Set generation parameters (optional)
GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig();
config.setTemperature(0.7);
config.setMaxOutputTokens(100);
request.setGenerationConfig(config);

// Call the Gemini model
GeminiResponse response = vertexAiClient.generateContent(request);

// Get the generated text
String generatedText = response.getGeneratedText();
```

## Testing

The project includes comprehensive tests that demonstrate how to mock both Google authentication and Vertex AI calls using WireMock.

Run the tests with:

```
./gradlew test
```

### Integration Testing

The `GeminiClientIntegrationTest` class shows how to set up a complete integration test that mocks both Google authentication and Vertex AI calls.

## Project Structure

- `src/main/java/org/poc/ai/`
  - `auth/` - Authentication services
  - `client/` - Vertex AI client implementation
  - `config/` - Configuration and environment loading
  - `model/` - Request and response models
  - `Main.java` - Example usage

- `src/test/java/org/poc/ai/`
  - `auth/` - Tests for authentication services
  - `client/` - Tests for Vertex AI client
  - `integration/` - Integration tests

## License

This project is licensed under the MIT License - see the LICENSE file for details.
