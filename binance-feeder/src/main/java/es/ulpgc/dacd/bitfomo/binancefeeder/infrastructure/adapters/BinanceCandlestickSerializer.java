package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports.CandlestickSerializer;

import java.time.format.DateTimeFormatter;

public class BinanceCandlestickSerializer implements CandlestickSerializer {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private final String source;

    public BinanceCandlestickSerializer(String source) {
        this.source = source;
    }

    @Override
    public String serializeCandlestick(Candlestick candlestick) {
        StringBuilder json = new StringBuilder("{");
        appendField(json, "ts", "\"" + FORMATTER.format(candlestick.ts()) + "\"");
        appendField(json, "ss", "\"" + source + "\"");
        appendField(json, "klineOpenTime", String.valueOf(candlestick.klineOpenTime()));
        appendField(json, "openPrice", String.valueOf(candlestick.openPrice()));
        appendField(json, "highPrice", String.valueOf(candlestick.highPrice()));
        appendField(json, "lowPrice", String.valueOf(candlestick.lowPrice()));
        appendField(json, "closePrice", String.valueOf(candlestick.closePrice()));
        appendField(json, "quoteAssetVolume", String.valueOf(candlestick.quoteAssetVolume()));
        appendField(json, "volume", String.valueOf(candlestick.volume()));
        appendField(json, "klineCloseTime", String.valueOf(candlestick.klineCloseTime()));
        appendField(json, "numberOfTrades", String.valueOf(candlestick.numberOfTrades()), true);
        json.append("}");
        return json.toString();
    }

    private void appendField(StringBuilder json, String key, String value) {
        json.append("\"").append(key).append("\":").append(value).append(",");
    }

    private void appendField(StringBuilder json, String key, String value, boolean last) {
        json.append("\"").append(key).append("\":").append(value);
    }
}