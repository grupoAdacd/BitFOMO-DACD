package com.bitfomo.domain.port;

import com.bitfomo.domain.model.CandlestickData;

public interface EventPublisherPort {
    void publish(CandlestickData kline);
}
