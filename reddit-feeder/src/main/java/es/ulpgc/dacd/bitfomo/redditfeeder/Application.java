package es.ulpgc.dacd.bitfomo.redditfeeder;

import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.*;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.ports.*;
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
        if (args.length < 3) {
            System.err.println("Usage: java -jar reddit-feeder.jar <userAgent> <brokerUrl> <queueName>");
            System.exit(1);
        }
        String userAgent = args[0];
        String brokerUrl = args[1];
        String queueName = args[2];

        ExternalRedditApiPort redditApi = new RedditApiAdapter(userAgent);
        EventPublisherPort publisher = new ActiveMqEventPublisher(brokerUrl, queueName);
        SentimentAnalyzerPort sentimentAnalyzer = new SentimentAnalyzer();

        FetchRedditPostsUseCase fetchPosts = new FetchRedditPostsUseCaseImpl(
                redditApi, SUBREDDITS, POST_LIMIT, publisher, sentimentAnalyzer);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                fetchPosts.fetchAndPublishPosts();
                logger.info("Fetch & persist completed successfully");
            } catch (Exception ex) {
                logger.error("Error in fetchAndPersistPosts", ex);
            }
        }, INITIAL_DELAY_SECONDS, PERIOD_MINUTES, TimeUnit.MINUTES);
    }
}