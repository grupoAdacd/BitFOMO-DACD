package es.ulpgc.dacd.bitfomo.redditfeeder.businessunitstuff;

public interface SentimentAnalyzerPort {
    Double analyzeSentiment(String text);
}