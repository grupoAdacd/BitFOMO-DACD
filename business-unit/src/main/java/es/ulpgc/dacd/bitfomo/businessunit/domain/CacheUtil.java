package es.ulpgc.dacd.bitfomo.businessunit.domain;

public interface CacheUtil {
    void insert();
    <T> T extract(String fileName);
}
