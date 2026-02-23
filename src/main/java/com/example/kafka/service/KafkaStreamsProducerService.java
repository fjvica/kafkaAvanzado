package com.example.kafka.service;

import com.example.kafka.Order;
import com.example.kafka.Payment;
import com.example.kafka.Balance;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaStreamsProducerService {

    private final KafkaTemplate<String, Order> orderKafkaTemplate;
    private final KafkaTemplate<String, Payment> paymentKafkaTemplate;
    private final KafkaTemplate<String, Balance> balanceKafkaTemplate;

    public KafkaStreamsProducerService(
            KafkaTemplate<String, Order> orderKafkaTemplate,
            KafkaTemplate<String, Payment> paymentKafkaTemplate,
            KafkaTemplate<String, Balance> balanceKafkaTemplate) {
        this.orderKafkaTemplate = orderKafkaTemplate;
        this.paymentKafkaTemplate = paymentKafkaTemplate;
        this.balanceKafkaTemplate = balanceKafkaTemplate;
    }

    /**
     * Envía datos de prueba a los topics: orders, payments y balances
     * usando CompletableFuture para manejar resultados y errores.
     */
    public void sendTestData() {
        for (int i = 1; i <= 10; i++) {

            // --- Order ---
            Order order = Order.newBuilder()
                    .setOrderId(i)
                    .setUserId("user" + (i % 3))
                    .setAmount(i * 50)
                    .build();

            CompletableFuture<SendResult<String, Order>> orderFuture =
                    orderKafkaTemplate.send("orders", order.getUserId().toString(), order);

            orderFuture.thenAccept(result -> System.out.println("Order enviada: " + order))
                    .exceptionally(ex -> {
                        System.err.println("Error enviando order: " + order + " - " + ex.getMessage());
                        return null;
                    });

            // --- Payment ---
            Payment payment = Payment.newBuilder()
                    .setPaymentId(i)
                    .setUserId("user" + (i % 3))
                    .setAmount(i * 50)
                    .build();

            CompletableFuture<SendResult<String, Payment>> paymentFuture =
                    paymentKafkaTemplate.send("payments", payment.getUserId().toString(), payment);


            paymentFuture.thenAccept(result -> System.out.println("Payment enviado: " + payment))
                    .exceptionally(ex -> {
                        System.err.println("Error enviando payment: " + payment + " - " + ex.getMessage());
                        return null;
                    });

            // --- Balance ---
            Balance balance = Balance.newBuilder()
                    .setUserId("user" + (i % 3))
                    .setBalance(1000.0 + i * 100)
                    .build();

            CompletableFuture<SendResult<String, Balance>> balanceFuture =
                    balanceKafkaTemplate.send("balances", balance.getUserId().toString(), balance);

            balanceFuture.thenAccept(result -> System.out.println("Balance enviado: " + balance))
                    .exceptionally(ex -> {
                        System.err.println("Error enviando balance: " + balance + " - " + ex.getMessage());
                        return null;
                    });
        }
    }
}