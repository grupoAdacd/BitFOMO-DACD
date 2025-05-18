package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.ExternalRedditApiPort;

import java.util.List;

public class RedditApi implements ExternalRedditApiPort {
    private final RedditConnection connection;
    private final RedditDeserializer deserializer;

    public RedditApi(RedditConnection connection, RedditDeserializer deserializer) {
        this.connection = connection;
        this.deserializer = deserializer;
    }

    @Override
    public List<RedditPost> fetchLatestPosts(List<String> subreddits, int limit) {
        return subreddits.stream()
                .flatMap(subreddit -> fetchPostsFromSubreddit(subreddit, limit).stream())
                .toList();
    }

    private List<RedditPost> fetchPostsFromSubreddit(String subreddit, int limit) {
        String jsonData = connection.fetchJsonData(subreddit, limit);
        return deserializer.parsePosts(jsonData);
    }
}