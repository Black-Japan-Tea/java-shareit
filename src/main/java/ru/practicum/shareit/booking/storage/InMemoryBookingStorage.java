package ru.practicum.shareit.booking.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookingStorage implements BookingStorage {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Booking createBooking(Booking booking) {
        booking.setId(idGenerator.getAndIncrement());
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Booking updateBooking(Booking booking) {
        bookings.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> getBookingById(Long bookingId) {
        return Optional.ofNullable(bookings.get(bookingId));
    }

    @Override
    public List<Booking> getAllBookingsByUser(Long userId) {
        return bookings.values().stream()
                .filter(booking -> booking.getBooker().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> getAllBookingsByOwner(Long ownerId) {
        return bookings.values().stream()
                .filter(booking -> booking.getItem().getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }
}