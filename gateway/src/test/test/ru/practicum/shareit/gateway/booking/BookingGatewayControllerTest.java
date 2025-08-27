package ru.practicum.shareit.gateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.booking.client.BookingClient;
import ru.practicum.shareit.gateway.booking.dto.BookingCreateDto;
import ru.practicum.shareit.gateway.booking.dto.BookingResponseDto;
import ru.practicum.shareit.gateway.booking.dto.BookingStatusDto;
import ru.practicum.shareit.gateway.exception.ConflictException;
import ru.practicum.shareit.gateway.exception.ForbiddenException;
import ru.practicum.shareit.gateway.exception.NotFoundException;
import ru.practicum.shareit.gateway.exception.ValidationException;
import ru.practicum.shareit.gateway.item.dto.ItemResponseDto;
import ru.practicum.shareit.gateway.user.dto.ShortUserResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingGatewayControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper mapper;

    private BookingResponseDto bookingResponseDto;
    private ItemResponseDto item;
    private ShortUserResponseDto booker;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        item = new ItemResponseDto();
        item.setId(1L);
        item.setName("Книга");
        item.setDescription("Книга по ремонту");
        item.setAvailable(true);

        booker = new ShortUserResponseDto();
        booker.setId(2L);
        booker.setName("Аркадий");

        bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(100L);
        bookingResponseDto.setStart(now.plusHours(1));
        bookingResponseDto.setEnd(now.plusHours(2));
        bookingResponseDto.setItem(item);
        bookingResponseDto.setBooker(booker);
        bookingResponseDto.setStatus(BookingStatusDto.WAITING);
    }

    @Test
    void createBooking_ValidData_ReturnsCreatedBooking() throws Exception {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingClient.createBooking(any(BookingCreateDto.class), eq(2L))).thenReturn(bookingResponseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.booker.name").value("Аркадий"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }


    @Test
    void updateBooking_Approved_ReturnsUpdatedBooking() throws Exception {
        BookingResponseDto initialBooking = new BookingResponseDto();
        initialBooking.setId(100L);
        initialBooking.setStart(LocalDateTime.now().plusDays(1));
        initialBooking.setEnd(LocalDateTime.now().plusDays(2));
        initialBooking.setItem(item);
        initialBooking.setBooker(booker);
        initialBooking.setStatus(BookingStatusDto.WAITING);

        BookingResponseDto updatedBooking = new BookingResponseDto();
        updatedBooking.setId(100L);
        updatedBooking.setStart(LocalDateTime.now().plusDays(1));
        updatedBooking.setEnd(LocalDateTime.now().plusDays(2));
        updatedBooking.setItem(item);
        updatedBooking.setBooker(booker);
        updatedBooking.setStatus(BookingStatusDto.APPROVED);

        when(bookingClient.updateBooking(eq(2L), eq(100L), eq(true)))
                .thenReturn(updatedBooking);

        mvc.perform(patch("/bookings/{bookingId}", 100L)
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_ValidId_ReturnsBooking() throws Exception {
        when(bookingClient.getBookingById(eq(2L), eq(100L))).thenReturn(bookingResponseDto);

        mvc.perform(get("/bookings/{bookingId}", 100L)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.booker.id").value(2L));
    }

    @Test
    void getAllBookingAtState_ReturnsList() throws Exception {
        when(bookingClient.getAllBookingAtState(eq(2L), eq("ALL")))
                .thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getAllOwnerBookingAtState_ReturnsList() throws Exception {
        when(bookingClient.getAllOwnerBookingAtState(eq(1L), eq("ALL")))
                .thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getAllBookingAtState_Waiting_ReturnsWaitingBookings() throws Exception {
        when(bookingClient.getAllBookingAtState(eq(1L), eq("WAITING")))
                .thenReturn(List.of(createBookingResponseDto(BookingStatusDto.WAITING)));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getAllBookingAtState_Approved_ReturnsApprovedBookings() throws Exception {
        when(bookingClient.getAllBookingAtState(eq(1L), eq("APPROVED")))
                .thenReturn(List.of(createBookingResponseDto(BookingStatusDto.APPROVED)));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    void getAllBookingAtState_Rejected_ReturnsRejectedBookings() throws Exception {
        when(bookingClient.getAllBookingAtState(eq(1L), eq("REJECTED")))
                .thenReturn(List.of(createBookingResponseDto(BookingStatusDto.REJECTED)));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("REJECTED"));
    }


    @Test
    void createBooking_InvalidData_ReturnsBadRequest() throws Exception {
        BookingCreateDto invalidDto = new BookingCreateDto();
        invalidDto.setStart(LocalDateTime.now().plusDays(1));
        invalidDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingClient.createBooking(any(BookingCreateDto.class), anyLong()))
                .thenThrow(new ValidationException("Неправильная дата бронирования"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBooking_Forbidden_ReturnsForbidden() throws Exception {
        when(bookingClient.updateBooking(eq(2L), eq(100L), eq(true)))
                .thenThrow(new ForbiddenException("У вас недостаточно прав подтвердить это бронирование"));

        mvc.perform(patch("/bookings/{bookingId}", 100L)
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBooking_Conflict_ReturnsConflict() throws Exception {
        BookingCreateDto conflictDto = new BookingCreateDto();
        conflictDto.setItemId(1L);
        conflictDto.setStart(LocalDateTime.now().plusDays(1));
        conflictDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingClient.createBooking(any(BookingCreateDto.class), anyLong()))
                .thenThrow(new ConflictException("Вещь на данный момент забронирована"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(conflictDto)))
                .andExpect(status().isConflict());
    }


    private BookingResponseDto createBookingResponseDto(BookingStatusDto status) {
        BookingResponseDto booking = new BookingResponseDto();
        booking.setId(100L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(new ItemResponseDto());
        booking.setBooker(new ShortUserResponseDto());
        booking.setStatus(status);
        return booking;
    }

    @Test
    void exceptionHandler_ValidationException_ReturnsBadRequest() throws Exception {
        when(bookingClient.createBooking(any(BookingCreateDto.class), anyLong()))
                .thenThrow(new ValidationException("Неправильная дата бронирования"));

        BookingCreateDto invalidDto = new BookingCreateDto();
        invalidDto.setStart(LocalDateTime.now().plusDays(1));
        invalidDto.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void notFoundExceptionHandler_NotFound_ReturnsNotFound() throws Exception {

        when(bookingClient.getBookingById(eq(1000L), eq(1000L)))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mvc.perform(get("/bookings/{bookingId}", 100L))
                .andExpect(status().isBadRequest());
    }
}