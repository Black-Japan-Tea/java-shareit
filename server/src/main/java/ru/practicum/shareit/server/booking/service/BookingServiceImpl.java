package ru.practicum.shareit.server.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.booking.dto.BookingCreateDto;
import ru.practicum.shareit.server.booking.dto.BookingMapper;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.dto.BookingStatusDto;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.repository.BookingRepository;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dal.ItemRepository;
import ru.practicum.shareit.server.item.dto.ItemMapper;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.dto.UserMapper;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final BookingStatus FIRST_BOOKING_STATUS = BookingStatus.WAITING;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public BookingResponseDto createBooking(BookingCreateDto dto, Long bookerId) {

        User booker = checkAndGetUser(bookerId);

        Item item = checkAndGetItem(dto.getItemId());

        if (!item.getAvailable()) {
            throw new ValidationException("Item with id=" + item.getId() + " not available");
        }


        Booking newBooking = BookingMapper.toBooking(booker, item, dto, FIRST_BOOKING_STATUS);


        return BookingMapper.toBookingResponseDto(bookingRepository.save(newBooking),
                ItemMapper.toItemResponseDto(newBooking.getItem()),
                UserMapper.toShortUserResponseDto(newBooking.getBooker()));
    }


    @Override
    public BookingResponseDto updateBooking(Long bookingId, Long bookerId, Boolean approved) {

        User booker = checkAndGetUser(bookerId);
        Booking booking = checkAndGetBooking(bookingId);

        if (!booking.getItem().getOwner().equals(booker.getId())) {
            throw new ForbiddenException("Only item owner can change booking approve");
        }

        if (booking.getStatus().equals(BookingStatus.WAITING)) {
            if (approved) {
                booking.setStatus(BookingStatus.APPROVED);
            } else {
                booking.setStatus(BookingStatus.REJECTED);
            }
        } else {
            throw new ValidationException("Booking can approved or rejected in WAITING status");
        }

        bookingRepository.save(booking);

        return BookingMapper.toBookingResponseDto(booking,
                ItemMapper.toItemResponseDto(booking.getItem()),
                UserMapper.toShortUserResponseDto(booking.getBooker()));
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {


        User user = checkAndGetUser(userId);
        Booking booking = checkAndGetBooking(bookingId);

        if (!(user.getId().equals(booking.getBooker().getId()) ||
              user.getId().equals(booking.getItem().getOwner()))) {
            throw new ForbiddenException("Only item owner or booker can get booking");
        }

        return BookingMapper.toBookingResponseDto(booking,
                ItemMapper.toItemResponseDto(booking.getItem()),
                UserMapper.toShortUserResponseDto(booking.getBooker()));
    }

    @Override
    public Collection<BookingResponseDto> getAllBookingAtState(Long userId, String state) {

        BookingStatusDto stateDTO = getBookingStatusDto(state);
        User booker = checkAndGetUser(userId);

        Collection<Booking> bookings = switch (stateDTO) {
            case ALL -> bookingRepository.findBookingsByBookerOrderByStartDesc(booker);
            case CURRENT -> bookingRepository.findCurrentBookings(booker);
            case PAST -> bookingRepository.findPastBookings(booker);
            case FUTURE -> bookingRepository.findFutureBookings(booker);
            default -> bookingRepository.findBookingsByBookerAndStatusOrderByStartDesc(
                    booker, BookingStatus.valueOf(stateDTO.name()));
        };

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingResponseDto(booking,
                        ItemMapper.toItemResponseDto(booking.getItem()),
                        UserMapper.toShortUserResponseDto(booking.getBooker())))
                .collect(Collectors.toSet());

    }


    @Override
    public Collection<BookingResponseDto> getAllOwnerBookingAtState(Long userId, String state) {
        BookingStatusDto stateDTO = getBookingStatusDto(state);
        User booker = checkAndGetUser(userId);

        Collection<Booking> bookings = switch (stateDTO) {
            case ALL -> bookingRepository.findAllBookingsByOwner(booker.getId());
            case CURRENT -> bookingRepository.findCurrentBookingsByOwner(booker.getId());
            case PAST -> bookingRepository.findPastBookingsByOwner(booker.getId());
            case FUTURE -> bookingRepository.findFutureBookingsByOwner(booker.getId());
            default -> bookingRepository.findBookingsByOwnerAndStatus(
                    booker.getId(), BookingStatus.valueOf(stateDTO.name()));
        };

        return bookings.stream()
                .map(booking -> BookingMapper.toBookingResponseDto(booking,
                        ItemMapper.toItemResponseDto(booking.getItem()),
                        UserMapper.toShortUserResponseDto(booking.getBooker())))
                .collect(Collectors.toSet());
    }


    private User checkAndGetUser(Long userId) {
        return userRepository.getUserById(userId)
                .orElseThrow(() -> new ForbiddenException("User with id=" + userId + " not found"));
    }

    private Item checkAndGetItem(Long itemId) {
        return itemRepository.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found"));
    }

    private Booking checkAndGetBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking with id=" + id + " not found"));
    }

    private BookingStatusDto getBookingStatusDto(String state) {
        BookingStatusDto stateDTO;
        try {
            stateDTO = BookingStatusDto.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid state= " + state);
        }
        return stateDTO;
    }
}