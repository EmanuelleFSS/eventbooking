package com.eventbooking.bookingservice.service;

import com.eventbooking.bookingservice.client.EventServiceClient;
import com.eventbooking.bookingservice.dto.BookingRequest;
import com.eventbooking.bookingservice.dto.BookingResponse;
import com.eventbooking.bookingservice.entity.Booking;
import com.eventbooking.bookingservice.entity.BookingStatus;
import com.eventbooking.bookingservice.exception.BookingNotFoundException;
import com.eventbooking.bookingservice.exception.SeatsUnavailableException;
import com.eventbooking.bookingservice.mapper.BookingMapper;
import com.eventbooking.bookingservice.messaging.BookingEventPublisher;
import com.eventbooking.bookingservice.messaging.EmailConfirmationPublisher;
import com.eventbooking.bookingservice.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private EventServiceClient eventServiceClient;
    @Mock private BookingEventPublisher bookingEventPublisher;
    @Mock private EmailConfirmationPublisher emailConfirmationPublisher;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequest request;
    private Booking booking;

    @BeforeEach
    void setUp() {
        request = new BookingRequest();
        request.setEventId(1L);
        request.setCustomerEmail("test@example.com");
        request.setSeatsBooked(2);

        booking = new Booking();
        booking.setId(10L);
        booking.setEventId(1L);
        booking.setCustomerEmail("test@example.com");
        booking.setSeatsBooked(2);
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    void createBooking_shouldReserveSeatsAndPersistBooking_whenEventServiceAccepts() {
        when(bookingMapper.toEntity(request)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(
                BookingResponse.builder().id(10L).status(BookingStatus.CONFIRMED).build());

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(eventServiceClient, times(1)).reserveSeats(1L, 2);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingEventPublisher, times(1)).publishBookingCreated(booking);
        verify(emailConfirmationPublisher, times(1)).publishConfirmationEmail(booking);
    }

    @Test
    void createBooking_shouldNotPersistBooking_whenEventServiceRejectsDueToInsufficientSeats() {
        doThrow(new SeatsUnavailableException(1L, 2))
                .when(eventServiceClient).reserveSeats(1L, 2);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(SeatsUnavailableException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(bookingEventPublisher, never()).publishBookingCreated(any());
        verify(emailConfirmationPublisher, never()).publishConfirmationEmail(any());
    }

    @Test
    void getBookingById_shouldReturnBooking_whenBookingExists() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toResponse(booking)).thenReturn(
                BookingResponse.builder().id(10L).customerEmail("test@example.com").build());

        BookingResponse response = bookingService.getBookingById(10L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCustomerEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getBookingById_shouldThrowException_whenBookingDoesNotExist() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(1L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void cancelBooking_shouldCancelBookingAndCallRepositorySave_whenBookingExists() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(
                BookingResponse.builder().id(10L).status(BookingStatus.CANCELLED).build());

        BookingResponse response = bookingService.cancelBooking(10L);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(eventServiceClient, times(1)).releaseSeats(1L, 2);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingEventPublisher, times(1)).publishBookingCancelled(booking);
    }
}
