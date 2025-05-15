package es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;

import java.util.List;

public interface PostRepositoryPort {
    void saveAll(List<RedditPost> posts);
}
