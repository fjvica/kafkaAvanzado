package com.example.kafka.service;

import com.example.kafka.Message;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    public void consume(Message message) {
        if (message.getId() % 5 == 0) { // ejemplo: fallar cada 5 mensajes
            throw new RuntimeException("Forzando error para DLT");
        }
        System.out.println("Recibido: " + message);
    }
}
