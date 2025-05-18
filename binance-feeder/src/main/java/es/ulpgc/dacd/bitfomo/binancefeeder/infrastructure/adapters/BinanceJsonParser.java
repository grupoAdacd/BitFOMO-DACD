package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.JsonParserProvider;
import org.json.JSONArray;
import org.json.JSONObject;

public class BinanceJsonParser implements JsonParserProvider {
    private final String json;

    public BinanceJsonParser(String json) {
        this.json = json;
    }

    @Override
    public JSONObject parseObject() {
        return new JSONObject(json);
    }

    @Override
    public JSONArray parseArray() {
        return new JSONArray(json);
    }
}