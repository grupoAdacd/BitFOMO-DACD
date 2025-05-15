package es.ulpgc.dacd.redditfeeder.infrastructure.ports;

import es.ulpgc.dacd.redditfeeder.domain.RedditPost;

import java.util.List;

public interface PostRepositoryPort {
    void saveAll(List<RedditPost> posts);
}
