package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking toBooking(BookingRequestDto bookingRequestDto);

    @Mapping(target = "booker", source = "booker")
    @Mapping(target = "item", source = "item")
    BookingResponseDto toBookingResponseDto(Booking booking);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "bookerId", source = "booker.id")
    BookingShortDto toBookingShortDto(Booking booking);
}