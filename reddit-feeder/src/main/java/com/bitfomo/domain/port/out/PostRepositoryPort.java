package com.bitfomo.domain.port.out;

import com.bitfomo.domain.model.RedditPost;
import java.util.List;

public interface PostRepositoryPort {
    void saveAll(List<RedditPost> posts);
}
