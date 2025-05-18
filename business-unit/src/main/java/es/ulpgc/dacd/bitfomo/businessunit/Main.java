package es.ulpgc.dacd.bitfomo.businessunit;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.CLIUserInterface;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.DatamartBuilder;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.RecommendationCalculator;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.RedditDeduplicator;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.BinanceEventProcessor;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.RedditEventProcessor;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.BinanceActiveMQListener;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.RedditActiveMQListener;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.BinanceFileEventReader;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.CsvDatamartWriter;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.PythonSentimentCalculator;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.RedditFileEventReader;

public class Main {
    public static void main(String[] args) {
        validateArguments(args);
        String csvPath = args[0];
        String redditPath = args[1];
        String binancePath = args[2];
        String activeMQUrl = args[3];
        String redditTopic = "RedditPost";
        String binanceTopic = "CryptoPrice";
        Controller controller = initializeController(csvPath, redditPath, binancePath, activeMQUrl, redditTopic, binanceTopic);
        controller.start(redditPath, binancePath);
    }

    private static void validateArguments(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: java -jar business-unit.jar <csvPath> <redditPath> <binancePath> <activeMQUrl>");
            System.exit(1);
        }
    }

    private static Controller initializeController(String csvPath, String redditPath, String binancePath,
                                                   String activeMQUrl, String redditTopic, String binanceTopic) {
        RedditDeduplicator deduplicator = new RedditDeduplicator();
        PythonSentimentCalculator sentimentCalculator = new PythonSentimentCalculator();
        RedditEventProcessor redditProcessor = new RedditEventProcessor(deduplicator, sentimentCalculator);
        BinanceEventProcessor binanceProcessor = new BinanceEventProcessor();
        RedditFileEventReader redditReader = new RedditFileEventReader();
        BinanceFileEventReader binanceReader = new BinanceFileEventReader();
        RedditActiveMQListener redditListener = new RedditActiveMQListener(activeMQUrl, redditTopic);
        BinanceActiveMQListener binanceListener = new BinanceActiveMQListener(activeMQUrl, binanceTopic);
        CsvDatamartWriter writer = new CsvDatamartWriter(csvPath);
        DatamartBuilder datamartBuilder = new DatamartBuilder(redditProcessor, binanceProcessor, writer);
        RecommendationCalculator calculator = new RecommendationCalculator();
        CLIUserInterface cli = new CLIUserInterface(csvPath, calculator);
        return new Controller(redditProcessor, binanceProcessor, datamartBuilder, cli, redditReader, binanceReader, redditListener, binanceListener);
    }
}