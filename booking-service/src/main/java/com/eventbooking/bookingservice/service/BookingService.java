package com.eventbooking.bookingservice.service;

import com.eventbooking.bookingservice.client.EventServiceClient;
import com.eventbooking.bookingservice.dto.BookingRequest;
import com.eventbooking.bookingservice.dto.BookingResponse;
import com.eventbooking.bookingservice.entity.Booking;
import com.eventbooking.bookingservice.entity.BookingStatus;
import com.eventbooking.bookingservice.exception.BookingNotFoundException;
import com.eventbooking.bookingservice.mapper.BookingMapper;
import com.eventbooking.bookingservice.messaging.BookingEventPublisher;
import com.eventbooking.bookingservice.messaging.EmailConfirmationPublisher;
import com.eventbooking.bookingservice.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final EventServiceClient eventServiceClient;
    private final BookingEventPublisher bookingEventPublisher;
    private final EmailConfirmationPublisher emailConfirmationPublisher;

    public BookingService(BookingRepository bookingRepository,
                          BookingMapper bookingMapper,
                          EventServiceClient eventServiceClient,
                          BookingEventPublisher bookingEventPublisher,
                          EmailConfirmationPublisher emailConfirmationPublisher) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.eventServiceClient = eventServiceClient;
        this.bookingEventPublisher = bookingEventPublisher;
        this.emailConfirmationPublisher = emailConfirmationPublisher;
    }

    public BookingResponse createBooking(BookingRequest request) {
        eventServiceClient.reserveSeats(request.getEventId(), request.getSeatsBooked());

        Booking booking = bookingMapper.toEntity(request);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(Instant.now());
        Booking savedBooking = bookingRepository.save(booking);

        bookingEventPublisher.publishBookingCreated(savedBooking);
        emailConfirmationPublisher.publishConfirmationEmail(savedBooking);

        return bookingMapper.toResponse(savedBooking);
    }

    public BookingResponse getBookingById(Long id) {
        Booking booking = findBookingOrThrow(id);
        return bookingMapper.toResponse(booking);
    }

    public BookingResponse cancelBooking(Long id) {
        Booking booking = findBookingOrThrow(id);

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);

        eventServiceClient.releaseSeats(booking.getEventId(), booking.getSeatsBooked());

        bookingEventPublisher.publishBookingCancelled(updatedBooking);

        return bookingMapper.toResponse(updatedBooking);
    }

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
}
