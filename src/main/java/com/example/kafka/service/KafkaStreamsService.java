package com.example.kafka.service;

import com.example.kafka.*;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class KafkaStreamsService {

    private static final Logger log = LoggerFactory.getLogger(KafkaStreamsService.class);

    private final SpecificAvroSerde<Order> orderSerde;
    private final SpecificAvroSerde<Payment> paymentSerde;
    private final SpecificAvroSerde<Balance> balanceSerde;
    private final SpecificAvroSerde<Alert> alertSerde;

    public KafkaStreamsService(SpecificAvroSerde<Order> orderSerde,
                               SpecificAvroSerde<Payment> paymentSerde,
                               SpecificAvroSerde<Balance> balanceSerde,
                               SpecificAvroSerde<Alert> alertSerde) {
        this.orderSerde = orderSerde;
        this.paymentSerde = paymentSerde;
        this.balanceSerde = balanceSerde;
        this.alertSerde = alertSerde;
    }

    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {

        // 1️⃣ Stream base de Orders con Log de entrada
        KStream<String, Order> orders = streamsBuilder.stream("orders",
                        Consumed.with(Serdes.String(), orderSerde))
                .peek((key, order) -> log.info("📥 [ORDERS SOURCE] Recibida Order ID: {} para User: {} con Amount: {}",
                        order.getOrderId(), key, order.getAmount()));

        // 2️⃣ Filtrar órdenes grandes (> 100)
        orders.filter((key, order) -> order.getAmount() > 100)
                .peek((key, order) -> log.info("🚀 [FILTER] Order {} supera los 100. Enviando a 'big-orders'.", order.getOrderId()))
                .to("big-orders", Produced.with(Serdes.String(), orderSerde));

        // 3️⃣ Agregación: Conteo total
        orders.groupBy((key, order) -> order.getUserId().toString(),
                        Grouped.with(Serdes.String(), orderSerde))
                .count(Materialized.as("orders-count-store"))
                .toStream()
                .peek((key, count) -> log.info("📊 [AGGREGATE] Usuario: {} ahora tiene un total de {} órdenes.", key, count))
                .to("aggregated-counts", Produced.with(Serdes.String(), Serdes.Long()));

        // 4️⃣ Ventanas de tiempo (10 segundos)
        orders.groupBy((key, order) -> order.getUserId().toString(),
                        Grouped.with(Serdes.String(), orderSerde))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
                .count()
                .toStream()
                .peek((key, count) -> log.info("🕒 [WINDOW] User: {} | Ventana: {} - {} | Count: {}",
                        key.key(), key.window().start(), key.window().end(), count))
                .to("windowed-counts",
                        Produced.with(WindowedSerdes.timeWindowedSerdeFrom(String.class, 10000L), Serdes.Long()));

        // 5️⃣ Stream de Payments y Table de Balances
        KStream<String, Payment> payments = streamsBuilder.stream("payments",
                        Consumed.with(Serdes.String(), paymentSerde))
                .peek((key, payment) -> log.info("💰 [PAYMENTS SOURCE] Recibido Pago ID: {} de User: {}", payment.getPaymentId(), key));

        KTable<String, Balance> balancesTable = streamsBuilder.table("balances",
                Consumed.with(Serdes.String(), balanceSerde));

        // 6️⃣ Join Enriquecido: Order + Balance (KStream-KTable Join)
        orders.leftJoin(
                balancesTable,
                (order, balance) -> {
                    double balanceAmount = (balance != null) ? balance.getBalance() : 0.0;
                    log.info("🤝 [JOIN KStream-KTable] Order: {} | Balance encontrado: {}", order.getOrderId(), balanceAmount);
                    return Alert.newBuilder()
                            .setUserId(order.getUserId())
                            .setMessage("OrderId=" + order.getOrderId() + ", Amount=" + order.getAmount() + ", Balance=" + balanceAmount)
                            .build();
                },
                Joined.with(Serdes.String(), orderSerde, balanceSerde)
        ).to("enriched-orders", Produced.with(Serdes.String(), alertSerde));

        // 7️⃣ Join de Eventos: Order + Payment (KStream-KStream Join)
        orders.join(
                payments,
                (order, payment) -> {
                    log.info("🔗 [JOIN KStream-KStream] ¡MATCH! Order {} y Payment {} coinciden.", order.getOrderId(), payment.getPaymentId());
                    return "OrderId=" + order.getOrderId() + " pagada con monto: " + payment.getAmount();
                },
                JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(5)),
                StreamJoined.with(Serdes.String(), orderSerde, paymentSerde)
        ).to("joined-orders-payments", Produced.with(Serdes.String(), Serdes.String()));

        // --- EXTRACCIÓN DE LA TOPOLOGÍA ---
        Topology topology = streamsBuilder.build();
        System.out.println("========================================");
        System.out.println("TOPOLOGÍA DE KAFKA STREAMS (Copia esto):");
        System.out.println(topology.describe());
        System.out.println("========================================");
    }
}