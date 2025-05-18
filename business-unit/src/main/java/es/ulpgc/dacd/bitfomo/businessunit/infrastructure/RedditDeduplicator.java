package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.Deduplicator;

import java.util.HashSet;
import java.util.Set;

public class RedditDeduplicator implements Deduplicator {
    private final Set<String> processedPostIds = new HashSet<>();

    public boolean isDuplicate(String id) {
        return processedPostIds.contains(id);
    }

    public void register(String id) {
        processedPostIds.add(id);
    }
}