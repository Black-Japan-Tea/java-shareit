package ru.practicum.shareit.server.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.booking.controller.BookingController;
import ru.practicum.shareit.server.booking.dto.BookingCreateDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.booking.service.BookingService;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.user.dto.ShortUserResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper mapper;

    private BookingResponseDto bookingResponseDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        ItemResponseDto item = new ItemResponseDto();
        item.setId(1L);
        item.setName("Book");
        item.setDescription("Interesting book");
        item.setAvailable(true);

        ShortUserResponseDto booker = new ShortUserResponseDto();
        booker.setId(2L);
        booker.setName("Alice");

        bookingResponseDto = new BookingResponseDto();
        bookingResponseDto.setId(100L);
        bookingResponseDto.setStart(now.plusHours(1));
        bookingResponseDto.setEnd(now.plusHours(2));
        bookingResponseDto.setItem(item);
        bookingResponseDto.setBooker(booker);
        bookingResponseDto.setStatus(BookingStatus.WAITING);
    }

    @Test
    void createBooking_ReturnsCreatedBooking() throws Exception {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(1L);
        dto.setStart(now.plusHours(1));
        dto.setEnd(now.plusHours(2));

        when(bookingService.createBooking(any(BookingCreateDto.class), eq(2L))).thenReturn(bookingResponseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingResponseDto.getId()))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.booker.name").value("Alice"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void updateBooking_Approved_ReturnsUpdatedBooking() throws Exception {

        bookingResponseDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.updateBooking(eq(100L), eq(1L), eq(true)))
                .thenReturn(bookingResponseDto);

        mvc.perform(patch("/bookings/{bookingId}", 100L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_ReturnsBooking() throws Exception {
        when(bookingService.getBookingById(eq(100L), eq(2L))).thenReturn(bookingResponseDto);

        mvc.perform(get("/bookings/{bookingId}", 100L)
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.booker.id").value(2L));
    }

    @Test
    void getAllBookingAtState_ReturnsList() throws Exception {
        when(bookingService.getAllBookingAtState(eq(2L), eq("ALL")))
                .thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void getAllOwnerBookingAtState_ReturnsList() throws Exception {
        when(bookingService.getAllOwnerBookingAtState(eq(1L), eq("ALL")))
                .thenReturn(List.of(bookingResponseDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
}