package com.bitfomo.domain;

import java.util.List;

public interface PostRepositoryPort {
    void saveAll(List<RedditPost> posts);
}
