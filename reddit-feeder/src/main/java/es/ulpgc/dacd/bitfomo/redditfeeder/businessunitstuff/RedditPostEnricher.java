package es.ulpgc.dacd.bitfomo.redditfeeder.businessunitstuff;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;

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
                post.ts(),
                post.subreddit(),
                post.author(),
                post.title(),
                post.selftext()
        );
    }
}