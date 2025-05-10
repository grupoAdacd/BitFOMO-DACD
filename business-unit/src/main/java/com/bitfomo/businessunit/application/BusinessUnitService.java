package com.bitfomo.businessunit.application;

import com.bitfomo.businessunit.domain.BusinessUnitServicePort;
import com.bitfomo.businessunit.domain.DatamartPort;

public class BusinessUnitService implements BusinessUnitServicePort {
    private final DatamartPort datamartPort;

    public BusinessUnitService(DatamartPort datamartPort) {
        this.datamartPort = datamartPort;
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
}