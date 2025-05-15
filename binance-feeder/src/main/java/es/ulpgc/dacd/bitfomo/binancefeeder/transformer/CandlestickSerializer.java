package es.ulpgc.dacd.bitfomo.binancefeeder.transformer;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import org.json.JSONObject;

public class CandlestickSerializer {
    public JSONObject serialize(Candlestick candleStick) {
        JSONObject candleStickObject = new JSONObject();
        candleStickObject.put("ts", candleStick.getTs());
        candleStickObject.put("ss", candleStick.getSs());
        candleStickObject.put("klineOpenTime", candleStick.getKlineOpenTime());
        candleStickObject.put("openPrice", candleStick.getOpenPrice());
        candleStickObject.put("highPrice", candleStick.getHighPrice());
        candleStickObject.put("lowPrice", candleStick.getLowPrice());
        candleStickObject.put("closePrice", candleStick.getClosePrice());
        candleStickObject.put("quoteAssetVolume", candleStick.getQuoteAssetVolume());
        candleStickObject.put("volume", candleStick.getVolume());
        candleStickObject.put("klineCloseTime", candleStick.getKlineCloseTime());
        candleStickObject.put("numberOfTrades", candleStick.getNumberOfTrades());
        return candleStickObject;
    }
}
