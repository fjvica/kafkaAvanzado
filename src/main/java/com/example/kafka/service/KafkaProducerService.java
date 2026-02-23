package com.example.kafka.service;

import com.example.kafka.Message;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessages() {
        for (int i = 1; i <= 100; i++) {
            Message msg = new Message(i, "Mensaje #" + i);

            CompletableFuture<SendResult<String, Message>> future =
                    kafkaTemplate.send("test-topic", String.valueOf(msg.getId()), msg);

            // Callback usando thenAccept / exceptionally
            future.thenAccept(result -> System.out.println("Enviado: " + msg))
                    .exceptionally(ex -> {
                        System.err.println("Error al enviar: " + msg + " - " + ex.getMessage());
                        return null;
                    });
        }
    }
}