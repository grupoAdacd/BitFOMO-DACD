package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports;

public interface SentimentAnalyzerPort {
    Double analyzeSentiment(String text);
}