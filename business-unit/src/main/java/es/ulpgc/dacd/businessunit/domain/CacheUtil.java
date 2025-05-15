package es.ulpgc.dacd.businessunit.domain;

public interface CacheUtil {
    void insert();
    <T> T extract(String fileName);
}
