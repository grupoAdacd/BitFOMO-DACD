package com.bitfomo.transformer.parser;
import com.bitfomo.domain.model.CandlestickData;
import org.json.*;

import java.util.ArrayList;

public interface JsonParserProvider {
    JSONObject parseObject() throws JSONException;
    JSONArray parseArray() throws JSONException;
    JSONArray deserializer(CandlestickData candleStick) throws JSONException;
}
