package es.ulpgc.dacd.bitfomo.binancefeeder.domain;

public interface ExchangeDataProvider {
    String fetchWhenInformation(long startDateTime, long endDateTime);
}
