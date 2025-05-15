package es.ulpgc.dacd.redditfeeder.infrastructure.adapters;

import es.ulpgc.dacd.redditfeeder.infrastructure.ports.SentimentAnalyzerPort;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

public class StanfordSentimentAnalyzer implements SentimentAnalyzerPort {
    private final StanfordCoreNLP pipeline;

    public StanfordSentimentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    @Override
    public Double analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0; // Neutral si el texto está vacío
        }

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);

        double totalScore = 0.0;
        int sentenceCount = 0;

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            totalScore += convertSentimentToScore(sentiment);
            sentenceCount++;
        }

        return sentenceCount > 0 ? totalScore / sentenceCount : 0.0;
    }

    private double convertSentimentToScore(String sentiment) {
        return switch (sentiment) {
            case "Very positive" -> 1.0;
            case "Positive" -> 0.5;
            case "Neutral" -> 0.0;
            case "Negative" -> -0.5;
            case "Very negative" -> -1.0;
            default -> 0.0;
        };
    }
}