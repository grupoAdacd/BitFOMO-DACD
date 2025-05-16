package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

public interface BusinessUnitServicePort {
    String generateRecommendation(String startTime, String endTime);
}