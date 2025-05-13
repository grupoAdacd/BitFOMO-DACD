package com.bitfomo.domain;

public interface KlineRepositoryPort {
    void save(CandlestickData kline);
    long getLastKline();
}