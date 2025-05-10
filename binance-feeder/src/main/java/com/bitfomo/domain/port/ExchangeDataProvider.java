package com.bitfomo.domain.port;

public interface ExchangeDataProvider {
    String fetchWhenInformation(long startDateTime, long endDateTime);
}
