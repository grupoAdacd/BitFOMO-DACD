package com.bitfomo.domain.model;

public class CandlestickData {
    private long KlineOpenTime;
    private String OpenPrice;
    private String HighPrice;
    private String LowPrice;
    private String ClosePrice;
    private String QuoteAssetVolume;
    private String Volume;
    private long KlineCloseTime;
    private int NumberOfTrades;

    public CandlestickData(long klineOpenTime, String openPrice, String highPrice, String lowPrice, String closePrice, String volume, long klineCloseTime, String quoteAssetVolume, int numberOfTrades) {
        KlineOpenTime = klineOpenTime;
        OpenPrice = openPrice;
        HighPrice = highPrice;
        LowPrice = lowPrice;
        ClosePrice = closePrice;
        QuoteAssetVolume = quoteAssetVolume;
        Volume = volume;
        KlineCloseTime = klineCloseTime;
        NumberOfTrades = numberOfTrades;
    }

    public long getKlineOpenTime() {
        return KlineOpenTime;
    }

    public void setKlineOpenTime(long klineOpenTime) {
        KlineOpenTime = klineOpenTime;
    }

    public String getOpenPrice() {
        return OpenPrice;
    }

    public void setOpenPrice(String openPrice) {
        OpenPrice = openPrice;
    }

    public String getHighPrice() {
        return HighPrice;
    }

    public void setHighPrice(String highPrice) {
        HighPrice = highPrice;
    }

    public String getLowPrice() {
        return LowPrice;
    }

    public void setLowPrice(String lowPrice) {
        LowPrice = lowPrice;
    }

    public String getQuoteAssetVolume() {
        return QuoteAssetVolume;
    }

    public void setQuoteAssetVolume(String quoteAssetVolume) {
        QuoteAssetVolume = quoteAssetVolume;
    }

    public String getClosePrice() {
        return ClosePrice;
    }

    public void setClosePrice(String closePrice) {
        ClosePrice = closePrice;
    }

    public String getVolume() {
        return Volume;
    }

    public void setVolume(String volume) {
        Volume = volume;
    }

    public long getKlineCloseTime() {
        return KlineCloseTime;
    }

    public void setKlineCloseTime(long klineCloseTime) {
        KlineCloseTime = klineCloseTime;
    }

    public int getNumberOfTrades() {
        return NumberOfTrades;
    }

    public void setNumberOfTrades(int numberOfTrades) {
        NumberOfTrades = numberOfTrades;
    }

    public double transformDouble(String parameter) {
        return Double.parseDouble(parameter);
    }
}
