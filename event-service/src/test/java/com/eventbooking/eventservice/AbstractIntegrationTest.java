package com.eventbooking.eventservice;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class AbstractIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @ServiceConnection
    protected static final KafkaContainer kafka = new KafkaContainer("apache/kafka:latest");

    static {
        postgres.start();
        kafka.start();
    }
}