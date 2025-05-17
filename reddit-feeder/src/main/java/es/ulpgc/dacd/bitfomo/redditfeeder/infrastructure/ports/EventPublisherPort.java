package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;

public interface EventPublisherPort {
    void publish(RedditPost post);
}
