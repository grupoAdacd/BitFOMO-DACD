package es.ulpgc.dacd.bitfomo.binancefeeder.domain;

import java.time.Instant;

public record Candlestick(
        Instant ts,
        long klineOpenTime,
        double openPrice,
        double highPrice,
        double lowPrice,
        double closePrice,
        double volume,
        long klineCloseTime,
        double quoteAssetVolume,
        int numberOfTrades
) {
    public Candlestick(long klineOpenTime, double openPrice, double highPrice, double lowPrice,
                       double closePrice, double volume, long klineCloseTime,
                       double quoteAssetVolume, int numberOfTrades) {
        this(Instant.ofEpochMilli(klineOpenTime), klineOpenTime, openPrice, highPrice,
                lowPrice, closePrice, volume, klineCloseTime, quoteAssetVolume, numberOfTrades);
    }
}