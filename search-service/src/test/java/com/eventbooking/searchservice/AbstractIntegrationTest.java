package com.eventbooking.searchservice;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

public abstract class AbstractIntegrationTest {

    @ServiceConnection
    protected static final MongoDBContainer mongodb = new MongoDBContainer("mongo:8");

    @ServiceConnection
    protected static final KafkaContainer kafka = new KafkaContainer("apache/kafka:latest");

    static {
        mongodb.start();
        kafka.start();
    }
}