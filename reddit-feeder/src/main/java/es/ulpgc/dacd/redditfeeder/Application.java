package es.ulpgc.dacd.redditfeeder;

import es.ulpgc.dacd.redditfeeder.infrastructure.adapters.ActiveMqEventPublisher;
import es.ulpgc.dacd.redditfeeder.infrastructure.adapters.JdbcPostRepository;
import es.ulpgc.dacd.redditfeeder.infrastructure.adapters.RedditApiAdapter;
import es.ulpgc.dacd.redditfeeder.infrastructure.adapters.StanfordSentimentAnalyzer;
import es.ulpgc.dacd.redditfeeder.infrastructure.FetchRedditPostsUseCaseImpl;
import es.ulpgc.dacd.redditfeeder.infrastructure.ports.EventPublisherPort;
import es.ulpgc.dacd.redditfeeder.infrastructure.ports.ExternalRedditApiPort;
import es.ulpgc.dacd.redditfeeder.infrastructure.FetchRedditPostsUseCase;
import es.ulpgc.dacd.redditfeeder.infrastructure.ports.PostRepositoryPort;
import es.ulpgc.dacd.redditfeeder.infrastructure.ports.SentimentAnalyzerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
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
        String jdbcUrl = args[1];
        String brokerUrl = args[2];
        String queueName = args[3];

        ExternalRedditApiPort redditApi = new RedditApiAdapter(userAgent);
        PostRepositoryPort postRepo = new JdbcPostRepository(jdbcUrl);
        EventPublisherPort publisher = new ActiveMqEventPublisher(brokerUrl, queueName);
        SentimentAnalyzerPort sentimentAnalyzer = new StanfordSentimentAnalyzer();

        FetchRedditPostsUseCase fetchPosts = new FetchRedditPostsUseCaseImpl(
                redditApi, postRepo, SUBREDDITS, POST_LIMIT, publisher, sentimentAnalyzer);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                fetchPosts.fetchAndPersistPosts();
                logger.info("Fetch & persist completed successfully");
            } catch (Exception ex) {
                logger.error("Error in fetchAndPersistPosts", ex);
            }
        }, INITIAL_DELAY_SECONDS, PERIOD_MINUTES, TimeUnit.MINUTES);
    }
}