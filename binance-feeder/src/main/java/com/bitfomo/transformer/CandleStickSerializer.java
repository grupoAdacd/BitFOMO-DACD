package com.bitfomo.transformer;

import com.bitfomo.domain.CandlestickData;
import org.json.JSONObject;

public class CandleStickSerializer {
    public JSONObject serialize(CandlestickData candleStick) {
        JSONObject candleStickObject = new JSONObject();
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
