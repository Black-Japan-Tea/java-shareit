package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingFilter;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;

    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        validateBookingDates(bookingDto);

        UserDto bookerDto = userService.getUserById(userId);
        User booker = userMapper.toUser(bookerDto);

        ItemDto itemDto = itemService.getItemById(userId, bookingDto.getItemId());
        Item item = itemMapper.toItem(itemDto);

        validateBookingCreation(booker, item);

        Booking booking = bookingMapper.toBooking(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingStorage.createBooking(booking);
        return bookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getExistingBooking(bookingId);
        validateBookingApproval(userId, booking);

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingStorage.updateBooking(booking);
        return bookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = getExistingBooking(bookingId);
        validateBookingAccess(userId, booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsByUser(Long userId, String state) {
        userService.getUserById(userId);
        BookingFilter filter = parseBookingFilter(state);

        List<Booking> bookings = getFilteredBookings(
                () -> bookingStorage.getAllBookingsByUser(userId),
                filter
        );

        return bookingMapper.toBookingDtoList(bookings);
    }

    @Override
    public List<BookingDto> getAllBookingsByOwner(Long userId, String state) {
        userService.getUserById(userId);
        BookingFilter filter = parseBookingFilter(state);

        List<Booking> bookings = getFilteredBookings(
                () -> bookingStorage.getAllBookingsByOwner(userId),
                filter
        );

        return bookingMapper.toBookingDtoList(bookings);
    }

    private Booking getExistingBooking(Long bookingId) {
        return bookingStorage.getBookingById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
    }

    private void validateBookingDates(BookingDto bookingDto) {
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new BookingValidationException("End date must be after start date");
        }
        if (bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new BookingValidationException("Dates cannot be equal");
        }
    }

    private void validateBookingCreation(User booker, Item item) {
        if (item.getOwner().getId().equals(booker.getId())) {
            throw new BookingValidationException("Owner cannot book own item");
        }
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Item is not available");
        }
    }

    private void validateBookingApproval(Long userId, Booking booking) {
        UserDto ownerDto = userService.getUserById(userId);
        if (!booking.getItem().getOwner().getId().equals(ownerDto.getId())) {
            throw new BookingAccessException("Only item owner can approve booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BookingValidationException("Booking is already processed");
        }
    }

    private void validateBookingAccess(Long userId, Booking booking) {
        UserDto userDto = userService.getUserById(userId);
        if (!booking.getBooker().getId().equals(userDto.getId()) &&
            !booking.getItem().getOwner().getId().equals(userDto.getId())) {
            throw new BookingAccessException("Access denied");
        }
    }

    private BookingFilter parseBookingFilter(String state) {
        try {
            return BookingFilter.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookingStateException("Unknown state: " + state);
        }
    }

    private List<Booking> getFilteredBookings(Supplier<List<Booking>> bookingsSupplier,
                                              BookingFilter filter) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingsSupplier.get();

        return switch (filter) {
            case ALL -> bookings;
            case CURRENT -> filterBookings(bookings,
                    b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now));
            case PAST -> filterBookings(bookings,
                    b -> b.getEnd().isBefore(now));
            case FUTURE -> filterBookings(bookings,
                    b -> b.getStart().isAfter(now));
            case WAITING -> filterBookings(bookings,
                    b -> b.getStatus() == BookingStatus.WAITING);
            case REJECTED -> filterBookings(bookings,
                    b -> b.getStatus() == BookingStatus.REJECTED);
            case CANCELED -> filterBookings(bookings,
                    b -> b.getStatus() == BookingStatus.CANCELED);
        };
    }

    private List<Booking> filterBookings(List<Booking> bookings, Predicate<Booking> filter) {
        return bookings.stream()
                .filter(filter)
                .collect(Collectors.toList());
    }
}