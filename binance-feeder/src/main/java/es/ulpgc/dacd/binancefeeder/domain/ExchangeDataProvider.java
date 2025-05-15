package es.ulpgc.dacd.binancefeeder.domain;

public interface ExchangeDataProvider {
    String fetchWhenInformation(long startDateTime, long endDateTime);
}
