package com.bitfomo.domain.port.out;

import com.bitfomo.domain.model.RedditPost;

public interface EventPublisherPort {
    void publish(RedditPost post);
}
