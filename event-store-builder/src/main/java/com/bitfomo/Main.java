package com.bitfomo;

import com.bitfomo.adapters.broker.MessageReceiver;
import jakarta.jms.JMSException;

import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws JMSException {
        MessageReceiver receiver = new MessageReceiver(Collections.singletonList("CryptoPrice"), "tcp://localhost:61616");
        receiver.start();
    }
}
