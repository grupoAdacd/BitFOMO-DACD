package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

import java.util.Map;

public interface DatamartPort {
    void storeRedditPost(Map<String, Object> postData);
    void storeCryptoPrice(Map<String, Object> priceData);
    void flushToCsv();
    double getAverageSentiment(String subreddit, String startTime, String endTime);
    double getAveragePrice(String symbol, String startTime, String endTime);
}