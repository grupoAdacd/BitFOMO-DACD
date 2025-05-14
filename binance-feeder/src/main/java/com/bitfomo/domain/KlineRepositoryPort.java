package com.bitfomo.domain;

public interface KlineRepositoryPort {
    void save(Candlestick kline);
    long getLastKline();
}