package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.SentimentAnalyzerPort;
import okhttp3.*;

import java.io.IOException;

public class SentimentAnalyzer implements SentimentAnalyzerPort {
    private static final String API_URL = "http://localhost:5000/analyze";
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public SentimentAnalyzer() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    @Override
    public Double analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0; // Neutral si el texto está vacío
        }

        try {
            String json = mapper.writeValueAsString(new TextRequest(text));
            RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Error en la solicitud a la API de análisis de sentimientos: " + response);
                    return 0.0;
                }

                JsonNode responseBody = mapper.readTree(response.body().string());
                return responseBody.get("sentimentScore").asDouble();
            }
        } catch (IOException e) {
            System.err.println("Error al analizar el sentimiento: " + e.getMessage());
            return 0.0;
        }
    }

    private static class TextRequest {
        private final String text;

        public TextRequest(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}