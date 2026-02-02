package com.translator;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/translate")
public class TranslatorResource {

    private static final String API_KEY = "AIzaSyAYMPs9zotwpEewwOj6D6b44bsh16Bm-Gg";
    
    private static final String MODEL_NAME = "gemini-2.5-flash"; 
    
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + API_KEY;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> translate(@QueryParam("word") String englishText) {
        Map<String, String> responseMap = new HashMap<>();

        if (englishText == null || englishText.trim().isEmpty()) {
            responseMap.put("error", "Please provide a word.");
            return responseMap;
        }

        try {
            // Prompt
            String prompt = "Translate this English text to Moroccan Darija. Output ONLY the translation. Text: " + englishText;

            // JSON Body
            String jsonBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + escapeJson(prompt) + "\" }] }] }";

            // Requête
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String translation = extractTranslationRegex(response.body());
                responseMap.put("original", englishText);
                responseMap.put("translation", translation);
                responseMap.put("success", "true");
            } else {
                responseMap.put("error", "Google Error: " + response.statusCode());
                responseMap.put("details", response.body());
                responseMap.put("success", "false");
                // Debug pour vous aider
                System.out.println("URL utilisée : " + GEMINI_URL);
                System.out.println("Réponse erreur : " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("error", "Server Error: " + e.getMessage());
            responseMap.put("success", "false");
        }

        return responseMap;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"").replace("\n", " ");
    }

    private String extractTranslationRegex(String jsonResponse) {
        Pattern pattern = Pattern.compile("\"text\":\\s*\"(.*?)\"");
        Matcher matcher = pattern.matcher(jsonResponse);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", " ").trim();
        }
        return "Translation not found";
    }
}