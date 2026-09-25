package com.eventbooking.notificationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "processed_notifications",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_booking_notification",
                columnNames = {"booking_id", "notification_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ProcessedNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;
}
