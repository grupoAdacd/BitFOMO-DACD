package com.bitfomo.application;

import com.bitfomo.domain.RedditPost;
import com.bitfomo.domain.FetchRedditPostsUseCase;
import com.bitfomo.domain.ExternalRedditApiPort;
import com.bitfomo.domain.PostRepositoryPort;
import com.bitfomo.domain.EventPublisherPort;

import java.util.List;

public class FetchRedditPostsUseCaseImpl implements FetchRedditPostsUseCase {

    private final ExternalRedditApiPort redditApi;
    private final PostRepositoryPort repository;
    private final List<String> subreddits;
    private final int limit;
    private final EventPublisherPort publisher;

    public FetchRedditPostsUseCaseImpl(
            ExternalRedditApiPort redditApi,
            PostRepositoryPort repository,
            List<String> subreddits,
            int limit,
            EventPublisherPort publisher
    ) {
        this.redditApi  = redditApi;
        this.repository = repository;
        this.subreddits = List.copyOf(subreddits);
        this.limit      = limit;
        this.publisher  = publisher;
    }

    @Override
    public void fetchAndPersistPosts() {
        List<RedditPost> posts = redditApi.fetchLatestPosts(subreddits, limit);
        repository.saveAll(posts);

        for (RedditPost post : posts) {
            publisher.publish(post);
        }
    }
}
