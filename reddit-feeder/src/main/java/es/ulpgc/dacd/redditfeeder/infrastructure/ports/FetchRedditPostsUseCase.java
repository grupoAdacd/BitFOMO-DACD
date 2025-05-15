package es.ulpgc.dacd.redditfeeder.infrastructure.ports;

public interface FetchRedditPostsUseCase {
    void fetchAndPersistPosts();
}
