package ru.practicum.shareit.booking.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ItemAccessDeniedException;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь не доступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("Вы не можете забронировать собственную вещь");
        }

        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть после даты начала");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                bookingRequestDto.getItemId(),
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd());

        if (!overlappingBookings.isEmpty()) {
            throw new ValidationException("Вещь уже забронирована на этот период");
        }

        Booking booking = bookingMapper.toBooking(bookingRequestDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("Только владелец вещи может подтвердить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найден"));

        if (!booking.getBooker().getId().equals(userId) &&
            !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("Доступ к информации по бронированию отклонён");
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentBookingsByBooker(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
                break;
            default:
                throw new ValidationException("Неизвестный статус: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long userId, String state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings;
        Sort sort = Sort.by(Sort.Direction.DESC, "start");

        switch (bookingState) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(userId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentBookingsByOwner(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
                break;
            default:
                throw new ValidationException("Неизвестный статус: " + state);
        }

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Неизвестный статус: " + state);
        }
    }
}