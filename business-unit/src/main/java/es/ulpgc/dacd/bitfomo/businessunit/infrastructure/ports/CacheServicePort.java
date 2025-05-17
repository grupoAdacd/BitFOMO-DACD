package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;


public interface CacheServicePort {
    void insertFromConsumer(String url);
    void insert(String folder);
    <T> T extract(String folderPath);
    <T> T process(String fileName);

}
