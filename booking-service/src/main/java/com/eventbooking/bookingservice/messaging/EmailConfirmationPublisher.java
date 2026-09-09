package com.eventbooking.bookingservice.messaging;

import com.eventbooking.bookingservice.config.RabbitMQConfig;
import com.eventbooking.bookingservice.entity.Booking;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailConfirmationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public EmailConfirmationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishConfirmationEmail(Booking booking) {
        EmailConfirmationMessage message = new EmailConfirmationMessage(
                booking.getCustomerEmail(),
                booking.getEventId(),
                booking.getSeatsBooked()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, message);
    }
}
