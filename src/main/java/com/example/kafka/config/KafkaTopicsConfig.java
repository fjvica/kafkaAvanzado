package com.example.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name("orders").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic enrichedOrders() {
        return TopicBuilder.name("enriched-orders").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentsTopic() {
        return TopicBuilder.name("payments").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic balancesTopic() {
        return TopicBuilder.name("balances").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic bigOrdersTopic() {
        return TopicBuilder.name("big-orders").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic aggregatedCountsTopic() {
        return TopicBuilder.name("aggregated-counts").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic windowedCountsTopic() {
        return TopicBuilder.name("windowed-counts").partitions(3).replicas(1).build();
    }
}