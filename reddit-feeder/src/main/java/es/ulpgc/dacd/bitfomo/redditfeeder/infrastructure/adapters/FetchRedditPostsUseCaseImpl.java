package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.RedditPostEnricher;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.*;
import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;

import java.util.List;

public class FetchRedditPostsUseCaseImpl implements FetchRedditPostsUseCase {
    private final ExternalRedditApiPort redditApi;
    private final PostRepositoryPort repository;
    private final List<String> subreddits;
    private final int limit;
    private final EventPublisherPort publisher;
    private final RedditPostEnricher postEnricher;

    public FetchRedditPostsUseCaseImpl(ExternalRedditApiPort redditApi, PostRepositoryPort repository,
                                       List<String> subreddits, int limit, EventPublisherPort publisher,
                                       SentimentAnalyzerPort sentimentAnalyzer) {
        this.redditApi = redditApi;
        this.repository = repository;
        this.subreddits = subreddits;
        this.limit = limit;
        this.publisher = publisher;
        this.postEnricher = new RedditPostEnricher(sentimentAnalyzer);
    }

    @Override
    public void fetchAndPersistPosts() {
        List<RedditPost> posts = redditApi.fetchLatestPosts(subreddits, limit);
        List<RedditPost> enrichedPosts = posts.stream()
                .map(postEnricher::enrichWithSentiment).toList();
        repository.saveAll(enrichedPosts);
        for (RedditPost post : enrichedPosts) {
            publisher.publish(post);
        }
    }
}