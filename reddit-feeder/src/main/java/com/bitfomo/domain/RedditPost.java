package com.bitfomo.domain;

import java.time.Instant;

public record RedditPost(
        String id,
        Instant timestamp,
        String subreddit,
        String author,
        String title,
        String body,
        Double sentimentScore
) {}
