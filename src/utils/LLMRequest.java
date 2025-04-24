package utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LLMRequest {
    private StringBuilder developerMessage;

    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String MODEL = "gpt-4.1-mini"; // Choose your desired model

    private final transient String API_KEY; // transient to prevent saving to external files

    public LLMRequest(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public void createPrompt(String desc, ArrayList<String> options) {
        StringBuilder developerMessage = new StringBuilder();
        developerMessage.append("Pretend you're a physics tutor. The student is solving a physics problem, and is given the following prompt: ");
        developerMessage.append(String.format("\"%s\". ", desc));
        developerMessage.append(String.format("The student has the following options available to select from. Only one is the correct one: %s. ", options.toString()));
        this.developerMessage = developerMessage;
    }

    public String requestHint() {
        if (API_KEY.isEmpty())
            return "No hint generated. Check to make sure a valid API key was used to launch the tutoring software.";

        StringBuilder message = new StringBuilder(developerMessage);
        message.append("The student has asked for a hint for this problem. Give one in no more than 2 sentences.");

        return sendRequest(message.toString());
    }

    public String requestSubmission(String submission) {
        if (API_KEY.isEmpty())
            return "No submission response generated. Check to make sure a valid API key was used to launch the tutoring software.";

        StringBuilder message = new StringBuilder(developerMessage);
        message.append(String.format("The student has found a solution to be: '%s'. " +
                "Give a short, 3 sentence explanation addressed to the student as to why this is correct or incorrect. " +
                "Be specific, and reveal the answer only after you have calculated the result. Remember, there is only " +
                "one correct answer of the options given to the student. When writing out your explanation, DO NOT Use " +
                "Latex to write out your equations. Instead, just use plain text.", submission));

        return sendRequest(message.toString());
    }

    private String sendRequest(String prompt) {
        try (HttpClient client = HttpClient.newHttpClient()){
            Gson gson = new Gson();

            // Create the request body as a JSON object
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", this.MODEL);

            JsonArray messages = new JsonArray();
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);
            requestBody.add("messages", messages);

            String requestBodyJson = gson.toJson(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse the JSON response to extract the assistant's message
                JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
                JsonArray choices = responseJson.getAsJsonArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JsonObject firstChoice = choices.get(0).getAsJsonObject();
                    JsonObject message = firstChoice.getAsJsonObject("message");
                    if (message != null && message.get("content") != null) {
                        return message.get("content").getAsString();
                    }
                }
                return "Error: Could not extract response content.";
            } else {
                return "Error: API request failed with status code " + response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            return "Error loading a response. Make sure you're connected to the internet.";
        }
    }
}
