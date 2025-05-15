package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.BusinessUnitServicePort;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.DatamartPort;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.EventStoreReaderPort;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class BusinessUnitService implements BusinessUnitServicePort {
    private final DatamartPort datamartPort;
    private final EventStoreReaderPort eventStoreReader;
    private final ObjectMapper mapper;

    public BusinessUnitService(DatamartPort datamartPort, EventStoreReaderPort eventStoreReader) {
        this.datamartPort = datamartPort;
        this.eventStoreReader = eventStoreReader;
        this.mapper = new ObjectMapper();
    }

    @Override
    public double calculateAverageSentiment(String subreddit, String startTime, String endTime) {
        return datamartPort.getAverageSentiment(subreddit, startTime, endTime);
    }

    @Override
    public double getAveragePrice(String symbol, String startTime, String endTime) {
        return datamartPort.getAveragePrice(symbol, startTime, endTime);
    }

    @Override
    public String generateRecommendation(String subreddit, String symbol, String startTime, String endTime) {
        double sentiment = calculateAverageSentiment(subreddit, startTime, endTime);
        double price = getAveragePrice(symbol, startTime, endTime);

        if (sentiment < 0.3 && price > 0) {
            return "Recomendación: Comprar " + symbol + ". Sentimiento bajo (" + sentiment + ") y precio promedio: " + price;
        }
        return "Recomendación: Mantener " + symbol + ". Sentimiento: " + sentiment + ", precio promedio: " + price;
    }

    @Override
    public double calculateHistoricalSentiment(String subreddit, String date) {
        List<String> events = eventStoreReader.readHistoricalEvents("RedditPost", "reddit-feeder", date);
        return calculateSentimentFromEvents(events, subreddit);
    }

    @Override
    public double getHistoricalPrice(String symbol, String date) {
        List<String> events = eventStoreReader.readHistoricalEvents("CryptoPrice", "binance-feeder", date);
        return calculatePriceFromEvents(events, symbol);
    }

    private double calculateSentimentFromEvents(List<String> events, String subreddit) {
        double totalSentiment = 0.0;
        int count = 0;

        for (String event : events) {
            try {
                Map<String, Object> postData = mapper.readValue(event, Map.class);
                String eventSubreddit = (String) postData.get("subreddit");
                Object sentimentScore = postData.get("sentimentScore");
                if (eventSubreddit != null && eventSubreddit.equals(subreddit) && sentimentScore != null) {
                    totalSentiment += ((Number) sentimentScore).doubleValue();
                    count++;
                }
            } catch (Exception e) {
                System.err.println("Error parsing RedditPost event: " + e.getMessage());
            }
        }

        return count > 0 ? totalSentiment / count : 0.0;
    }

    private double calculatePriceFromEvents(List<String> events, String symbol) {
        double totalPrice = 0.0;
        int count = 0;

        for (String event : events) {
            try {
                Map<String, Object> priceData = mapper.readValue(event, Map.class);
                if ("BTC".equals(symbol) && priceData.get("closePrice") != null) {
                    totalPrice += Double.parseDouble((String) priceData.get("closePrice"));
                    count++;
                }
            } catch (Exception e) {
                System.err.println("Error parsing CryptoPrice event: " + e.getMessage());
            }
        }

        return count > 0 ? totalPrice / count : 0.0;
    }
}