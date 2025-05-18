package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

public interface EventReader {
    void readEvents(String folderPath, EventProcessor processor);
}