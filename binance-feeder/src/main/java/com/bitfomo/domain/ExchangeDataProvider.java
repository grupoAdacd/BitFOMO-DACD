package com.bitfomo.domain;

public interface ExchangeDataProvider {
    String fetchWhenInformation(long startDateTime, long endDateTime);
}
