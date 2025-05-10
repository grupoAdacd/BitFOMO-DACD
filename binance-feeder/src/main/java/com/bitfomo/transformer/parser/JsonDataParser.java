package com.bitfomo.transformer.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
}
