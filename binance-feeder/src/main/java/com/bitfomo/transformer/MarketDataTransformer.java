package com.bitfomo.transformer;

import com.bitfomo.transformer.parser.JsonDataParser;
import com.bitfomo.domain.model.CandlestickData;

import java.util.ArrayList;

public class MarketDataTransformer {
    public ArrayList<CandlestickData> processRawToObject(String eventJsonString) {
        if (!eventJsonString.isEmpty()) {
            try {
                JsonDataParser eventParser = new JsonDataParser(eventJsonString);
                ArrayList<CandlestickData> allKlinesObject = new ArrayList<>();
                for (int i = 0; i < eventParser.parseArray().length(); i++) {
                    JsonDataParser Kline = new JsonDataParser(eventParser.parseArray().getJSONArray(i).toString());
                    if (Kline.parseArray().length() >= 10){
                        CandlestickData binanceKline = new CandlestickData(
                                Kline.parseArray().getLong(0),
                                Kline.parseArray().getString(1),
                                Kline.parseArray().getString(2),
                                Kline.parseArray().getString(3),
                                Kline.parseArray().getString(4),
                                Kline.parseArray().getString(5),
                                Kline.parseArray().getLong(6),
                                Kline.parseArray().getString(7),
                                Kline.parseArray().getInt(8)
                        );
                        allKlinesObject.add(binanceKline);
                    }
                }
                return allKlinesObject;
            } catch (Exception e){
                System.err.println("Error parsing JSON: "+e.getMessage());
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}
