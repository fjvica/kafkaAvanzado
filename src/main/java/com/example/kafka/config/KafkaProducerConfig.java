package com.example.kafka.config;

import com.example.kafka.Balance;
import com.example.kafka.Message;
import com.example.kafka.Order;
import com.example.kafka.Payment;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {


    // ---------------- Message ----------------
    @Bean
    public KafkaTemplate<String, Message> messageKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(Message.class));
    }

    // ---------------- Order ----------------
    @Bean
    public KafkaTemplate<String, Order> orderKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(Order.class));
    }

    // ---------------- Payment ----------------
    @Bean
    public KafkaTemplate<String, Payment> paymentKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(Payment.class));
    }

    // ---------------- Balance ----------------
    @Bean
    public KafkaTemplate<String, Balance> balanceKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(Balance.class));
    }

    // Método genérico de ProducerFactory
    private <T> ProducerFactory<String, T> producerFactory(Class<T> type) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("acks", "all");
        props.put("enable.idempotence", true);
        return new DefaultKafkaProducerFactory<>(props);
    }
}