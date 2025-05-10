package com.bitfomo.domain;

public interface EventPublisherPort {
    void publish(CandlestickData kline);
}
