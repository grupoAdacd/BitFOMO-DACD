package es.ulpgc.dacd.redditfeeder.infrastructure.ports;

import es.ulpgc.dacd.redditfeeder.domain.RedditPost;

public interface EventPublisherPort {
    void publish(RedditPost post);
}
