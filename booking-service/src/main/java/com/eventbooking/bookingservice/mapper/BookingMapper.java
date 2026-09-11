package com.eventbooking.bookingservice.mapper;

import com.eventbooking.bookingservice.dto.BookingRequest;
import com.eventbooking.bookingservice.dto.BookingResponse;
import com.eventbooking.bookingservice.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toEntity(BookingRequest request);

    BookingResponse toResponse(Booking booking);
}
