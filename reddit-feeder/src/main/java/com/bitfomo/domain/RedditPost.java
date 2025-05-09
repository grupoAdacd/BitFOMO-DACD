package com.bitfomo.domain;

import java.time.Instant;

public record RedditPost(
        String id,
        String ts,
        String ss,
        String subreddit,
        String author,
        String title,
        String selftext,
        Double sentimentScore,
        Instant timestamp
) {
    public RedditPost(String id, Instant timestamp, String subreddit, String author, String title, String selftext, Double sentimentScore) {
        this(
                id,
                timestamp.toString(),
                "reddit-feeder",
                subreddit,
                author,
                title,
                selftext,
                sentimentScore,
                timestamp
        );
    }
}