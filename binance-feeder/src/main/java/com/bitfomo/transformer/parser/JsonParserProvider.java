package com.bitfomo.transformer.parser;
import com.bitfomo.domain.model.CandlestickData;
import org.json.*;

public interface JsonParserProvider {
    JSONObject parseObject() throws JSONException;
    JSONArray parseArray() throws JSONException;
    JSONArray serializer(CandlestickData candleStick) throws JSONException;
}
