package es.ulpgc.dacd.bitfomo.binancefeeder.transformer;
import org.json.*;

public interface JsonParserProvider {
    JSONObject parseObject() throws JSONException;
    JSONArray parseArray() throws JSONException;
}
