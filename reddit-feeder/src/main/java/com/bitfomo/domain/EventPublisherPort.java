package com.bitfomo.domain;

public interface EventPublisherPort {
    void publish(RedditPost post);
}
