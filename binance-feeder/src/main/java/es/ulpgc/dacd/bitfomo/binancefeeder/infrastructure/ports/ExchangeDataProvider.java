package es.ulpgc.dacd.bitfomo.binancefeeder.infrastructure.ports;

public interface ExchangeDataProvider {
    String fetchWhenInformation(long startTime, long endTime);
}