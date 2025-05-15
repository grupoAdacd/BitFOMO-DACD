package es.ulpgc.dacd.bitfomo.redditfeeder.domain;

import java.time.Instant;

public record RedditPost(
        String id,
        String ts, //TODO Instant
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