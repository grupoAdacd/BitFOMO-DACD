package com.bitfomo;

import com.bitfomo.adapters.persistence.JdbcPostRepository;
import com.bitfomo.adapters.reddit.RedditApiAdapter;
import com.bitfomo.application.usecase.FetchRedditPostsUseCaseImpl;
import com.bitfomo.domain.port.in.FetchRedditPostsUseCase;
import com.bitfomo.domain.port.out.ExternalRedditApiPort;
import com.bitfomo.domain.port.out.PostRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final List<String> SUBREDDITS = List.of("Bitcoin", "CryptoCurrency", "CryptoMarkets");
    private static final int POST_LIMIT = 50;
    private static final int INITIAL_DELAY_SECONDS = 0;
    private static final int PERIOD_MINUTES = 5;

    public static void main(String[] args) {
        String userAgent = System.getenv().getOrDefault("REDDIT_USER_AGENT",
                "java:com.bitfomo:v1.0 (by /u/Shot_Picture8375)");

        ExternalRedditApiPort redditApi = new RedditApiAdapter(userAgent);
        PostRepositoryPort    postRepo  = new JdbcPostRepository("jdbc:sqlite:reddit.db");

        FetchRedditPostsUseCase fetchPosts =
                new FetchRedditPostsUseCaseImpl(redditApi, postRepo, SUBREDDITS, POST_LIMIT);

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                fetchPosts.fetchAndPersistPosts();
                logger.info("Fetch and persist completed successfully");
            } catch (Exception ex) {
                logger.error("Error during fetchAndPersistPosts", ex);
            }
        }, INITIAL_DELAY_SECONDS, PERIOD_MINUTES, TimeUnit.MINUTES);
    }
}
