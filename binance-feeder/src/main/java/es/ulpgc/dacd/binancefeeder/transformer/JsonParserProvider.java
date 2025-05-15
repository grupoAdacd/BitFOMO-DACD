package es.ulpgc.dacd.binancefeeder.transformer;
import org.json.*;

public interface JsonParserProvider {
    JSONObject parseObject() throws JSONException;
    JSONArray parseArray() throws JSONException;
}
