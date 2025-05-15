package es.ulpgc.dacd.redditfeeder.infrastructure.ports;

public interface SentimentAnalyzerPort {
    Double analyzeSentiment(String text);
}