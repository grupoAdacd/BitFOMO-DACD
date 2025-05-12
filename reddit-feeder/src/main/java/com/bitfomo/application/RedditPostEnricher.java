package com.bitfomo.application;

import com.bitfomo.domain.RedditPost;
import com.bitfomo.domain.SentimentAnalyzerPort;

public class RedditPostEnricher {
    private final SentimentAnalyzerPort sentimentAnalyzer;

    public RedditPostEnricher(SentimentAnalyzerPort sentimentAnalyzer) {
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    public RedditPost enrichWithSentiment(RedditPost post) {
        Double sentimentScore = sentimentAnalyzer.analyzeSentiment(post.selftext());
        return new RedditPost(
                post.id(),
                post.timestamp(),
                post.subreddit(),
                post.author(),
                post.title(),
                post.selftext(),
                sentimentScore
        );
    }
}