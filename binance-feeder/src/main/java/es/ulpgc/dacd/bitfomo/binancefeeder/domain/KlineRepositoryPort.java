package es.ulpgc.dacd.bitfomo.binancefeeder.domain;

public interface KlineRepositoryPort {
    void save(Candlestick kline);
    long getLastKline();
}