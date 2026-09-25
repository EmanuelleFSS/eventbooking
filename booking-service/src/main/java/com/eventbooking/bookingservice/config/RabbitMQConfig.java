package com.eventbooking.bookingservice.config;

import com.eventbooking.bookingservice.messaging.EmailConfirmationMessage;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "booking.email-confirmation";

    @Bean
    public Queue emailConfirmationQueue() {
        return new Queue(EMAIL_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("com.eventbooking");
        Map<String, Class<?>> idClassMapping = Map.of(
                "emailConfirmationMessage", EmailConfirmationMessage.class
        );
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.afterPropertiesSet();  // ← construit le mapping inverse (classe → alias)
        converter.setClassMapper(classMapper);
        return converter;
    }
}
