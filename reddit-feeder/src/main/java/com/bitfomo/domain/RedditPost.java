package com.bitfomo.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;

public record RedditPost(
        String id,
        Instant timestamp,  // Este será el ts (timestamp)
        String subreddit,   // Este es el subreddit
        String author,
        String title,
        String selftext,
        Double sentimentScore
) {

    // Convertir el RedditPost en un evento con ts y ss
    public String toEventJson() {
        String ss = "reddit";  // El subreddit siempre será "reddit" para ss

        // Crear el evento con ts, ss, y los demás atributos del RedditPost
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Creamos un objeto JSON con ts, ss y el subreddit
            JsonNode eventJson = objectMapper.createObjectNode();
            ((ObjectNode) eventJson).put("ts", this.timestamp.toString());  // timestamp -> ts
            ((ObjectNode) eventJson).put("ss", ss);  // El subreddit siempre será "reddit" para ss
            ((ObjectNode) eventJson).put("subreddit", this.subreddit);  // Agregar el subreddit

            // Agregamos los demás atributos del RedditPost
            ((ObjectNode) eventJson).put("id", this.id);
            ((ObjectNode) eventJson).put("author", this.author);
            ((ObjectNode) eventJson).put("title", this.title);
            ((ObjectNode) eventJson).put("selftext", this.selftext);
            if (this.sentimentScore != null) {
                ((ObjectNode) eventJson).put("sentimentScore", this.sentimentScore);
            }

            // Convertimos el evento a JSON y lo retornamos
            return objectMapper.writeValueAsString(eventJson);
        } catch (Exception e) {
            throw new RuntimeException("Error creando el evento JSON", e);
        }
    }
}
