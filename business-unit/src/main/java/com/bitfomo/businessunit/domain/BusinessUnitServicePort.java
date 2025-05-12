package com.bitfomo.businessunit.domain;

public interface BusinessUnitServicePort {
    double calculateAverageSentiment(String subreddit, String startTime, String endTime);
    double getAveragePrice(String symbol, String startTime, String endTime);
    String generateRecommendation(String subreddit, String symbol, String startTime, String endTime);
    double calculateHistoricalSentiment(String subreddit, String date);
    double getHistoricalPrice(String symbol, String date);
}