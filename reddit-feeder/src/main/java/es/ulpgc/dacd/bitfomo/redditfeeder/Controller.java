package es.ulpgc.dacd.bitfomo.redditfeeder;

import es.ulpgc.dacd.bitfomo.redditfeeder.domain.RedditPost;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.EventPublisherPort;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.ExternalRedditApiPort;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller {
    private final ExternalRedditApiPort redditApi;
    private final EventPublisherPort publisher;
    private final List<String> subreddits;
    private final int limit;
    private final ScheduledExecutorService scheduler;

    public Controller(ExternalRedditApiPort redditApi, EventPublisherPort publisher, List<String> subreddits, int limit) {
        this.redditApi = redditApi;
        this.publisher = publisher;
        this.subreddits = subreddits;
        this.limit = limit;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void execute() {
        Runnable task = this::fetchPosts;
        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.MINUTES);
        System.out.println("Reddit Controller started - Fetching every 5 minutes");
    }

    private void fetchPosts() {
        try {
            System.out.println("Starting Reddit fetch cycle");
            List<RedditPost> posts = redditApi.fetchLatestPosts(subreddits, limit);
            publishPosts(posts);
            System.out.println("Fetch cycle completed");
        } catch (Exception e) {
            System.err.println("Error during fetch cycle: " + e.getMessage());
        }
    }

    private void publishPosts(List<RedditPost> posts) {
        for (RedditPost post : posts) {
            try {
                publisher.publish(post);
                System.out.println("Published post: " + post.id());
            } catch (Exception e) {
                System.err.println("Error publishing post " + post.id() + ": " + e.getMessage());
            }
        }
    }
}