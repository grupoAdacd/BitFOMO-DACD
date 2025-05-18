package es.ulpgc.dacd.bitfomo.redditfeeder;

import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.MessagePublisher;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.RedditConnection;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.RedditDeserializer;
import es.ulpgc.dacd.bitfomo.redditfeeder.infrastructure.adapters.RedditApi;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java -jar reddit-feeder.jar <userAgent> <subreddits> <limit> <brokerUrl> <topicName>");
            System.exit(1);
        }

        String userAgent = args[0];
        List<String> subreddits = parseSubreddits(args[1]);
        int limit = Integer.parseInt(args[2]);
        String brokerUrl = args[3];
        String topicName = args[4];

        RedditConnection redditConnection = new RedditConnection(userAgent);
        RedditDeserializer redditDeserializer = new RedditDeserializer();
        RedditApi redditApi = new RedditApi(redditConnection, redditDeserializer);
        MessagePublisher publisher = new MessagePublisher(brokerUrl, topicName);
        Controller controller = new Controller(redditApi, publisher, subreddits, limit);
        controller.execute();
    }

    private static List<String> parseSubreddits(String subredditsArg) {
        return Arrays.asList(subredditsArg.split(","));
    }
}