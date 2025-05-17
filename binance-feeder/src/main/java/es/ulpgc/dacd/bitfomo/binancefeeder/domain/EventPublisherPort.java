package es.ulpgc.dacd.bitfomo.binancefeeder.domain;

public interface EventPublisherPort {
    void publish(Candlestick kline);
}
