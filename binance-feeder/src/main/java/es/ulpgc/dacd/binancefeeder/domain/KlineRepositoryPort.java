package es.ulpgc.dacd.binancefeeder.domain;

public interface KlineRepositoryPort {
    void save(Candlestick kline);
    long getLastKline();
}