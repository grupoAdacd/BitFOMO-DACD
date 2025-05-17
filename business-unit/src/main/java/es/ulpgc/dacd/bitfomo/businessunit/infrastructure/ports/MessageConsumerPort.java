package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

public interface MessageConsumerPort {
    <T> T startConsuming();
}