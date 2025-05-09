package com.bitfomo.domain;

import java.util.List;

public interface ExternalRedditApiPort {
    List<RedditPost> fetchLatestPosts(List<String> subreddits, int limit);
}
