package com.bitfomo.businessunit.domain;

//import com.bitfomo.domain.CryptoPrice;
import com.bitfomo.domain.RedditPost;

public interface DatamartPort {
    void storeRedditPost(RedditPost post);
    //void storeCryptoPrice(CryptoPrice price);
    void flushToCsv(); // Escribe los datos acumulados al CSV
    double getAverageSentiment(String subreddit, String startTime, String endTime);
    double getAveragePrice(String symbol, String startTime, String endTime);
}