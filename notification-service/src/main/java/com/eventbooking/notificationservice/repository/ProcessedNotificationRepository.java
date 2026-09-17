package com.eventbooking.notificationservice.repository;

import com.eventbooking.notificationservice.entity.NotificationType;
import com.eventbooking.notificationservice.entity.ProcessedNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedNotificationRepository extends JpaRepository<ProcessedNotification, Long> {

    boolean existsByBookingIdAndNotificationType(Long bookingId, NotificationType notificationType);
}
