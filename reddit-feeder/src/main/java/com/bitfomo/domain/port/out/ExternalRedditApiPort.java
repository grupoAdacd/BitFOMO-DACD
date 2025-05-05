package com.bitfomo.domain.port.out;

import com.bitfomo.domain.model.RedditPost;
import java.util.List;

public interface ExternalRedditApiPort {
    List<RedditPost> fetchLatestPosts(List<String> subreddits, int limit);
}
