package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;

import java.util.List;

public interface ExternalRedditApiPort {
    List<RedditPost> fetchLatestPosts(List<String> subreddits, int limit);
}
