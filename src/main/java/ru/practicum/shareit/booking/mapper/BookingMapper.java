package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    BookingDto toBookingDto(Booking booking);

    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    Booking toBooking(BookingDto bookingDto);
}