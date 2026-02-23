package com.example.kafka.config;

import com.example.kafka.Order;
import com.example.kafka.Payment;
import com.example.kafka.Balance;
import com.example.kafka.Alert;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Value("${spring.kafka.streams.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    private <T extends org.apache.avro.specific.SpecificRecord> SpecificAvroSerde<T> createAvroSerde() {
        SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
        serde.configure(Map.of(
                "schema.registry.url", schemaRegistryUrl,
                "specific.avro.reader", "true"
        ), false);
        return serde;
    }

    @Bean public SpecificAvroSerde<Order> orderSerde() { return createAvroSerde(); }
    @Bean public SpecificAvroSerde<Payment> paymentSerde() { return createAvroSerde(); }
    @Bean public SpecificAvroSerde<Balance> balanceSerde() { return createAvroSerde(); }
    @Bean public SpecificAvroSerde<Alert> alertSerde() { return createAvroSerde(); }
}