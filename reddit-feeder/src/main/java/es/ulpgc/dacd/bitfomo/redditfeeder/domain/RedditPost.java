package es.ulpgc.dacd.bitfomo.redditfeeder.domain;

import java.time.Instant;

public record RedditPost(
        String id,
        Instant ts,
        String ss,
        String subreddit,
        String author,
        String title,
        String selftext,
        Double sentimentScore
) {
    public RedditPost(String id, Instant createdUtc, String subreddit, String author, String title, String selftext, Double sentimentScore) {
        this(
                id,
                createdUtc,
                "reddit-feeder",
                subreddit,
                author,
                title,
                selftext,
                sentimentScore
        );
    }
}