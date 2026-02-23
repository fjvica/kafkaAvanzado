package com.example.kafka.config;

import com.example.kafka.Message;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Message> kafkaTemplate) {

        // DeadLetterPublishingRecoverer que apunta a topic-dlt
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + "-dlt", record.partition())
        );

        // Reintenta 3 veces con delay de 1s antes de enviarlo al DLT
        FixedBackOff backOff = new FixedBackOff(1000L, 3);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}