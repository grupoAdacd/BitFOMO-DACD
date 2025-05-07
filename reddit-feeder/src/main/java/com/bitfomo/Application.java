package com.bitfomo;

import com.bitfomo.adapters.persistence.JdbcPostRepository;
import com.bitfomo.adapters.broker.ActiveMqEventPublisher;
import com.bitfomo.adapters.reddit.RedditApiAdapter;
import com.bitfomo.application.usecase.FetchRedditPostsUseCaseImpl;
import com.bitfomo.domain.port.in.FetchRedditPostsUseCase;
import com.bitfomo.domain.port.out.ExternalRedditApiPort;
import com.bitfomo.domain.port.out.EventPublisherPort;
import com.bitfomo.domain.port.out.PostRepositoryPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("MM/dd/yyyy hh:mm:ss a")
            .withZone(ZoneId.systemDefault());
    private static final List<String> SUBREDDITS =
            List.of("Bitcoin", "CryptoCurrency", "CryptoMarkets");
    private static final int POST_LIMIT = 50;
    private static final int INITIAL_DELAY_SECONDS = 0;
    private static final int PERIOD_MINUTES = 5;

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java -jar reddit-feeder.jar <userAgent> <jdbcUrl> <brokerUrl> <queueName>");
            System.exit(1);
        }

        String userAgent = args[0];
        String jdbcUrl   = args[1];
        String brokerUrl = args[2];
        String queueName = args[3];

        ExternalRedditApiPort redditApi =
                new RedditApiAdapter(userAgent);
        PostRepositoryPort postRepo =
                new JdbcPostRepository(jdbcUrl);
        EventPublisherPort publisher =
                new ActiveMqEventPublisher(brokerUrl, queueName);

        FetchRedditPostsUseCase fetchPosts =
                new FetchRedditPostsUseCaseImpl(
                        redditApi, postRepo, SUBREDDITS, POST_LIMIT, publisher);

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                fetchPosts.fetchAndPersistPosts();
                logger.info("Fetch & persist completed successfully");
                // opcional: mostrar posts con timestamp formateado ...
            } catch (Exception ex) {
                logger.error("Error in fetchAndPersistPosts", ex);
            }
        }, INITIAL_DELAY_SECONDS, PERIOD_MINUTES, TimeUnit.MINUTES);
    }
}
