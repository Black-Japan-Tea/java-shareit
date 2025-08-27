package ru.practicum.shareit.server.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.server.booking.dto.BookingCreateDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.repository.BookingRepository;
import ru.practicum.shareit.server.booking.service.BookingServiceImpl;
import ru.practicum.shareit.server.exception.ForbiddenException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.exception.ValidationException;
import ru.practicum.shareit.server.item.dal.ItemRepository;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private User itemOwner;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {

        booker = new User();
        booker.setId(1L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        itemOwner = new User();
        itemOwner.setId(2L);
        itemOwner.setName("itemOwner");
        itemOwner.setEmail("itemOwner@example.com");


        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(2L);

        booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));
    }


    @Test
    void createBookingTest() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));


        when(userRepository.getUserById(anyLong()))
                .thenReturn(Optional.of(booker));

        when(itemRepository.getItemById(anyLong()))
                .thenReturn(Optional.of(item));

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(dto, booker.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
        assertEquals(booking.getStatus(), result.getStatus());
    }

    @Test
    void createBookingItemNotAvailableValidationException() {
        item.setAvailable(false);
        when(itemRepository.getItemById(anyLong())).thenReturn(Optional.of(item));
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(dto, booker.getId()));
    }


    @Test
    void createBookingUserNotFound() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.empty());

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(ForbiddenException.class, () ->
                bookingService.createBooking(dto, 999L));
    }

    @Test
    void createBookingItemNotFound() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.getItemById(anyLong())).thenReturn(Optional.empty());

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(999L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(dto, booker.getId()));
    }


    @Test
    void updateBookingTest() {
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(itemOwner));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBooking(booking.getId(), itemOwner.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void updateBookingRejectTest() {
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(itemOwner));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.updateBooking(booking.getId(), itemOwner.getId(), false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void updateBookingNotOwnerTest() {
        booking.setStatus(BookingStatus.WAITING);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));

        assertThrows(ForbiddenException.class, () ->
                bookingService.updateBooking(booking.getId(), 999L, true));
    }

    @Test
    void updateBookingInvalidStatusTest() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));

        assertThrows(ForbiddenException.class, () ->
                bookingService.updateBooking(booking.getId(), item.getOwner(), true));
    }


    @Test
    void getBookingByIdTest() {
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));

        BookingResponseDto result = bookingService.getBookingById(booking.getId(), booker.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBookingByIdUserIsNotOwnerOrBookerTest() {

        User badUser = new User();
        badUser.setId(999L);
        badUser.setName("badsUser");
        badUser.setEmail("badsUser@example.com");


        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(badUser));

        assertThrows(ForbiddenException.class, () ->
                bookingService.getBookingById(booking.getId(), 999L));
    }


    @Test
    void getAllBookingAtStateTest() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findBookingsByBookerOrderByStartDesc(any(User.class)))
                .thenReturn(List.of(booking));

        Collection<BookingResponseDto> result = bookingService.getAllBookingAtState(booker.getId(), "ALL");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAllBookingAtStateCURRENT() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentBookings(any(User.class))).thenReturn(List.of(booking));

        Collection<BookingResponseDto> result = bookingService.getAllBookingAtState(booker.getId(), "CURRENT");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }


    @Test
    void getAllBookingAtStatePAST() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));

        Collection<BookingResponseDto> result = bookingService.getAllBookingAtState(booker.getId(), "PAST");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void getAllBookingAtStateFUTURE() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));

        Collection<BookingResponseDto> result = bookingService.getAllBookingAtState(booker.getId(), "FUTURE");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllBookingAtStateInvalidStateTest() {
        assertThrows(ValidationException.class, () ->
                bookingService.getAllBookingAtState(booker.getId(), "UNKNOWN"));
    }


    @Test
    void getAllOwnerBookingAtStateTest() {
        when(userRepository.getUserById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllBookingsByOwner(anyLong()))
                .thenReturn(List.of(booking));

        Collection<BookingResponseDto> result = bookingService.getAllOwnerBookingAtState(booker.getId(), "ALL");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getAllOwnerBookingAtStateInvalidState() {
        assertThrows(ValidationException.class, () ->
                bookingService.getAllOwnerBookingAtState(booker.getId(), "UNKNOWN"));
    }
}