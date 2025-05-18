package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import java.util.HashSet;
import java.util.Set;

public class Deduplicator {
    private final Set<String> redditPostIds;

    public Deduplicator() {
        this.redditPostIds = new HashSet<>();
    }

    public boolean isDuplicateRedditPost(String postId) {
        return redditPostIds.contains(postId);
    }

    public void addRedditPostId(String postId) {
        redditPostIds.add(postId);
    }
}