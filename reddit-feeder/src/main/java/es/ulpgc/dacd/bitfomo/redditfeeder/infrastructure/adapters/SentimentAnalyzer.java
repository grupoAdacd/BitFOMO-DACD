package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import com.aliasi.classify.Classification;
import com.aliasi.classify.DynamicLMClassifier;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.SentimentAnalyzerPort;

public class SentimentAnalyzer implements SentimentAnalyzerPort {
    private final DynamicLMClassifier classifier;

    public SentimentAnalyzer() {
        // Carga un modelo básico para clasificación de sentimientos
        String[] categories = {"positive", "negative", "neutral"};
        int nGramSize = 8;
        this.classifier = DynamicLMClassifier.createNGramProcess(categories, nGramSize);
    }

    @Override
    public Double analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0; // Neutral si el texto está vacío
        }

        try {
            Classification classification = classifier.classify(text);
            String sentiment = classification.bestCategory();

            return switch (sentiment) {
                case "positive" -> 0.5;
                case "negative" -> -0.5;
                case "neutral" -> 0.0;
                default -> 0.0;
            };
        } catch (Exception e) {
            System.err.println("Error analyzing sentiment: " + e.getMessage());
            return 0.0; // Fallback a neutral en caso de error
        }
    }
}