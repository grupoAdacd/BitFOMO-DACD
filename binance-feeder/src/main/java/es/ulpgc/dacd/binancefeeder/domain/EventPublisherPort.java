package es.ulpgc.dacd.binancefeeder.domain;

public interface EventPublisherPort {
    void publish(Candlestick kline);
}
