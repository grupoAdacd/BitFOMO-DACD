package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.CandlestickParser;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.JsonParserProvider;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class BinanceCandlestickParser implements CandlestickParser {
    private final JsonParserProvider jsonParserProvider;
    private String currentJson;

    public BinanceCandlestickParser(JsonParserProvider jsonParserProvider) {
        this.jsonParserProvider = jsonParserProvider;
    }

    @Override
    public List<Candlestick> parseCandlesticks(String json) {
        this.currentJson = json;
        if (json == null || json.isEmpty() || json.trim().equals("[]")) {
            System.out.println("JSON vacío o array vacío recibido");
            return new ArrayList<>();
        }
        try {
            JsonParserProvider parser = new BinanceJsonParser(json);
            JSONArray array = parser.parseArray();
            if (array.length() == 0) {
                System.out.println("Array de candlesticks vacío");
                return new ArrayList<>();
            }
            List<Candlestick> candlesticks = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                parseSingleCandlestick(array, i, candlesticks);
            }
            return candlesticks;
        } catch (Exception e) {
            System.err.println("Error parseando candlesticks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void parseSingleCandlestick(JSONArray array, int index, List<Candlestick> candlesticks) {
        JsonParserProvider klineParser = new BinanceJsonParser(array.getJSONArray(index).toString());
        JSONArray klineArray = klineParser.parseArray();
        if (klineArray.length() >= 10) {
            Candlestick candlestick = createCandlestick(klineArray);
            candlesticks.add(candlestick);
        }
    }

    private Candlestick createCandlestick(JSONArray klineArray) {
        return new Candlestick(
                klineArray.getLong(0),
                klineArray.getDouble(1),
                klineArray.getDouble(2),
                klineArray.getDouble(3),
                klineArray.getDouble(4),
                klineArray.getDouble(5),
                klineArray.getLong(6),
                klineArray.getDouble(7),
                klineArray.getInt(8)
        );
    }
}