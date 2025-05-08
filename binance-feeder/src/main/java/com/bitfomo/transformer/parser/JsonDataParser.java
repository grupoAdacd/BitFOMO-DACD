package com.bitfomo.transformer.parser;

import com.bitfomo.domain.model.CandlestickData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonDataParser implements JsonParserProvider {
    private final String jsonString;

    public JsonDataParser(String jsonString) {
        this.jsonString = jsonString;
    }

    @Override
    public JSONObject parseObject() throws JSONException {
        return new JSONObject(this.jsonString);
    }

    @Override
    public JSONArray parseArray() throws JSONException {
        return new JSONArray(this.jsonString);
    }

    @Override
    public JSONArray serializer(CandlestickData candleStick) throws JSONException {
        ArrayList<Object> candleStickArray = new ArrayList<>();
        candleStickArray.add("Kline Open Time:"+ candleStick.getKlineOpenTime());
        candleStickArray.add("Open Price: " + candleStick.getOpenPrice());
        candleStickArray.add("High Price: "+ candleStick.getHighPrice());
        candleStickArray.add("Low Price: "+ candleStick.getLowPrice());
        candleStickArray.add("Close Price: "+ candleStick.getClosePrice());
        candleStickArray.add("Quote Asset Volume: "+ candleStick.getQuoteAssetVolume());
        candleStickArray.add("Volume: "+ candleStick.getVolume());
        candleStickArray.add("Kline Close Time: "+ candleStick.getKlineCloseTime());
        candleStickArray.add("Number of Trades: "+ candleStick.getNumberOfTrades());

        return new JSONArray(candleStickArray);
    }
}
