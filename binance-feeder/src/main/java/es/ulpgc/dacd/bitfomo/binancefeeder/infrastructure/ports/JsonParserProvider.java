package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports;

import org.json.JSONArray;
import org.json.JSONObject;

public interface JsonParserProvider {
    JSONObject parseObject();
    JSONArray parseArray();
}