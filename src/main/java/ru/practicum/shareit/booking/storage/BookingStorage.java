package ru.practicum.shareit.booking.storage;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingStorage {
    Booking createBooking(Booking booking);

    Booking updateBooking(Booking booking);

    Optional<Booking> getBookingById(Long bookingId);

    List<Booking> getAllBookingsByUser(Long userId);

    List<Booking> getAllBookingsByOwner(Long ownerId);
}