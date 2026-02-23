package com.example.kafka;

import com.example.kafka.service.KafkaProducerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KafkaRunner implements CommandLineRunner {

    private final KafkaProducerService producer;

    public KafkaRunner(KafkaProducerService producer) {
        this.producer = producer;
    }

    @Override
    public void run(String... args) throws Exception {
        producer.sendMessages();
    }
}
