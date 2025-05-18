package es.ulpgc.dacd.bitfomo.binancefeeder;

import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.BinanceCandlestickFetcher;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.LastKlineTimeManager;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters.ActiveMQCandlestickPublisher;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters.BinanceCandlestickParser;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters.BinanceCandlestickSerializer;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters.BinanceEndpointResolver;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters.BinanceHttpClient;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters.BinanceJsonParser;

public class Main {
    public static void main(String[] args) {
        validateArguments(args);
        String brokerUrl = args[0];
        String topicName = args[1];
        String symbol = args[2];
        String interval = args[3];
        int limit = Integer.parseInt(args[4]);
        long fetchPeriodSeconds = Long.parseLong(args[5]);
        String lastKlineFilePath = args[6];
        long initialRangeMillis = Long.parseLong(args[7]);
        Controller controller = initializeController(brokerUrl, topicName, symbol, interval, limit,
                fetchPeriodSeconds, lastKlineFilePath, initialRangeMillis);
        controller.start();
    }

    private static void validateArguments(String[] args) {
        if (args.length < 8) {
            System.err.println("Usage: java -jar binance-feeder.jar <brokerUrl> <topicName> <symbol> <interval> <limit> <fetchPeriodSeconds> <lastKlineFilePath> <initialRangeMillis>");
            System.exit(1);
        }
    }

    private static Controller initializeController(String brokerUrl, String topicName, String symbol,
                                                   String interval, int limit, long fetchPeriodSeconds,
                                                   String lastKlineFilePath, long initialRangeMillis) {
        LastKlineTimeManager timeManager = new LastKlineTimeManager(lastKlineFilePath);
        BinanceEndpointResolver endpointResolver = new BinanceEndpointResolver(symbol, interval, limit);
        BinanceHttpClient httpClient = new BinanceHttpClient(endpointResolver);
        BinanceJsonParser jsonParser = new BinanceJsonParser("");
        BinanceCandlestickParser candlestickParser = new BinanceCandlestickParser(jsonParser);
        BinanceCandlestickSerializer serializer = new BinanceCandlestickSerializer("binance-feeder");
        ActiveMQCandlestickPublisher publisher = new ActiveMQCandlestickPublisher(brokerUrl, topicName, serializer);
        BinanceCandlestickFetcher fetcher = new BinanceCandlestickFetcher(httpClient, endpointResolver, candlestickParser,
                timeManager, 60 * 1000L, initialRangeMillis);
        return new Controller(fetcher, publisher, fetchPeriodSeconds);
    }
}