package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.SentimentAnalyzerPort;

public class RedditPostEnricher {
    private final SentimentAnalyzerPort sentimentAnalyzer;

    public RedditPostEnricher(SentimentAnalyzerPort sentimentAnalyzer) {
        this.sentimentAnalyzer = sentimentAnalyzer;
    }

    public RedditPost enrichWithSentiment(RedditPost post) {
        String combinedText = post.title() != null ? post.title() : "";
        if (post.selftext() != null && !post.selftext().trim().isEmpty()) {
            combinedText += " " + post.selftext();
        }

        Double sentimentScore = sentimentAnalyzer.analyzeSentiment(combinedText.trim());
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