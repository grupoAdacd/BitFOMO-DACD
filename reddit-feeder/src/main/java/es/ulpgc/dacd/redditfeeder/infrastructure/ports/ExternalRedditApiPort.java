package es.ulpgc.dacd.redditfeeder.infrastructure.ports;

import es.ulpgc.dacd.redditfeeder.domain.RedditPost;

import java.util.List;

public interface ExternalRedditApiPort {
    List<RedditPost> fetchLatestPosts(List<String> subreddits, int limit);
}
