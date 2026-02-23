package com.example.kafka.service;

import com.example.kafka.Message;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessagesTransactional() {
        kafkaTemplate.executeInTransaction(operations -> {
            for (int i = 1; i <= 100; i++) {
                Message msg = new Message(i, "Mensaje #" + i);
                operations.send("test-topic", String.valueOf(msg.getId()), msg);
                System.out.println("Enviando: " + msg);

                if (i == 50) {
                    throw new RuntimeException("Error simulado en el mensaje 50");
                }
            }
            return true;
        });
    }

    public void sendOneTransactional(Message msg) {
        kafkaTemplate.executeInTransaction(ops -> {
            ops.send("test-topic", String.valueOf(msg.getId()), msg);
            return null;
        });
    }
}