package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

public interface MessageListener {
    void startListening(EventProcessor processor);
}