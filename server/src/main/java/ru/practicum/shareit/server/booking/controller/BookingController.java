package ru.practicum.shareit.server.booking.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.booking.dto.BookingCreateDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.service.BookingService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class BookingController {

    private final BookingService service;

    @PostMapping
    BookingResponseDto createBooking(@RequestBody
                                     @Valid
                                     BookingCreateDto newBookingData,
                                     @RequestHeader("X-Sharer-User-Id")
                                     Long userId) {
        return service.createBooking(newBookingData, userId);
    }


    @PatchMapping("{bookingId}")
    BookingResponseDto updateBooking(@RequestHeader("X-Sharer-User-Id")
                                     Long userId,
                                     @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        return service.updateBooking(bookingId, userId, approved);
    }


    @GetMapping("{bookingId}")
    BookingResponseDto getBookingById(@RequestHeader("X-Sharer-User-Id")
                                      Long userId,
                                      @PathVariable Long bookingId) {
        return service.getBookingById(bookingId, userId);
    }

    @GetMapping
    Collection<BookingResponseDto> getAllBookingAtState(
            @RequestHeader("X-Sharer-User-Id")
            Long userId,
            @RequestParam(defaultValue = "ALL")
            String state
    ) {
        return service.getAllBookingAtState(userId, state);
    }

    @GetMapping("/owner")
    Collection<BookingResponseDto> getAllOwnerBookingAtState(
            @RequestHeader("X-Sharer-User-Id")
            Long userId,
            @RequestParam(defaultValue = "ALL")
            String state
    ) {
        return service.getAllOwnerBookingAtState(userId, state);
    }
}