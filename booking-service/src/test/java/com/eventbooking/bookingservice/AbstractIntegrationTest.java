package com.eventbooking.bookingservice;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

public abstract class AbstractIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @ServiceConnection
    protected static final KafkaContainer kafka = new KafkaContainer("apache/kafka:latest");

    @ServiceConnection
    protected static final RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:4-management");

    static {
        postgres.start();
        kafka.start();
        rabbitmq.start();
    }
}