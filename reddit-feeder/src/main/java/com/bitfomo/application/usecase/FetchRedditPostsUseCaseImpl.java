package com.bitfomo.application.usecase;

import com.bitfomo.domain.model.RedditPost;
import com.bitfomo.domain.port.in.FetchRedditPostsUseCase;
import com.bitfomo.domain.port.out.ExternalRedditApiPort;
import com.bitfomo.domain.port.out.PostRepositoryPort;

import java.util.List;

public class FetchRedditPostsUseCaseImpl implements FetchRedditPostsUseCase {

    private final ExternalRedditApiPort redditApi;
    private final PostRepositoryPort repository;
    private final List<String> subreddits;
    private final int limit;

    public FetchRedditPostsUseCaseImpl(
            ExternalRedditApiPort redditApi,
            PostRepositoryPort repository,
            List<String> subreddits,
            int limit
    ) {
        this.redditApi  = redditApi;
        this.repository = repository;
        this.subreddits = List.copyOf(subreddits);
        this.limit      = limit;
    }

    @Override
    public void fetchAndPersistPosts() {
        List<RedditPost> posts = redditApi.fetchLatestPosts(subreddits, limit);
        repository.saveAll(posts);
    }
}
