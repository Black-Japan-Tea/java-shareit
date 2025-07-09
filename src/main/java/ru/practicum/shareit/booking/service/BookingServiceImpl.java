package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper bookingMapper;

    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        userService.getUserById(userId);

        Item item = itemService.getItemById(userId, bookingDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new BookingValidationException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь недоступна для бронирования");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new BookingValidationException("Дата окончания бронирования не может быть раньше даты начала");
        }

        if (bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new BookingValidationException("Даты бронирования не могут совпадать");
        }

        Booking booking = bookingMapper.toBooking(bookingDto);
        booking.setBooker(userService.getUserById(userId));
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingStorage.createBooking(booking);
        return bookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingStorage.getBookingById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new BookingAccessException("Подтверждать бронирование может только владелец вещи");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new BookingValidationException("Бронирование уже было подтверждено или отклонено");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingStorage.updateBooking(booking);
        return bookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingStorage.getBookingById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
            !booking.getItem().getOwner().getId().equals(userId)) {
            throw new BookingAccessException("Просматривать бронирование может только автор или владелец");
        }

        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookingsByUser(Long userId, String state) {
        userService.getUserById(userId);

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingStorage.getAllBookingsByUser(userId);
            case "CURRENT" -> bookingStorage.getAllBookingsByUser(userId).stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .collect(Collectors.toList());
            case "PAST" -> bookingStorage.getAllBookingsByUser(userId).stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .collect(Collectors.toList());
            case "FUTURE" -> bookingStorage.getAllBookingsByUser(userId).stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .collect(Collectors.toList());
            case "WAITING" -> bookingStorage.getAllBookingsByUser(userId).stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                    .collect(Collectors.toList());
            case "REJECTED" -> bookingStorage.getAllBookingsByUser(userId).stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                    .collect(Collectors.toList());
            default -> throw new BookingStateException("Unknown state: " + state);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllBookingsByOwner(Long userId, String state) {
        userService.getUserById(userId);

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        bookings = switch (state.toUpperCase()) {
            case "ALL" -> bookingStorage.getAllBookingsByOwner(userId);
            case "CURRENT" -> bookingStorage.getAllBookingsByOwner(userId).stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .collect(Collectors.toList());
            case "PAST" -> bookingStorage.getAllBookingsByOwner(userId).stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .collect(Collectors.toList());
            case "FUTURE" -> bookingStorage.getAllBookingsByOwner(userId).stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .collect(Collectors.toList());
            case "WAITING" -> bookingStorage.getAllBookingsByOwner(userId).stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.WAITING))
                    .collect(Collectors.toList());
            case "REJECTED" -> bookingStorage.getAllBookingsByOwner(userId).stream()
                    .filter(b -> b.getStatus().equals(BookingStatus.REJECTED))
                    .collect(Collectors.toList());
            default -> throw new BookingStateException("Unknown state: " + state);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}