package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

public interface SentimentCalculator {
    double calculateSentiment(String text);
}