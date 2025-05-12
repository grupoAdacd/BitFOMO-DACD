package com.bitfomo.domain;

public interface SentimentAnalyzerPort {
    Double analyzeSentiment(String text);
}