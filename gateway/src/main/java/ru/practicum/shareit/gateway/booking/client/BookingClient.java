package ru.practicum.shareit.gateway.booking.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookingCreateDto;
import ru.practicum.shareit.gateway.booking.dto.BookingResponseDto;

import java.util.Collection;


@FeignClient(
        url = "${shareit-server.url}",
        name = "booking-client",
        path = "/bookings")
public interface BookingClient {

    @PostMapping
    BookingResponseDto createBooking(
            @RequestBody @Valid BookingCreateDto newBookingData,
            @RequestHeader("X-Sharer-User-Id") Long userId);

    @PatchMapping("/{bookingId}")
    BookingResponseDto updateBooking(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable("bookingId") Long bookingId,
            @RequestParam("approved") Boolean approved);

    @GetMapping("/{bookingId}")
    BookingResponseDto getBookingById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable("bookingId") Long bookingId);

    @GetMapping
    Collection<BookingResponseDto> getAllBookingAtState(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state);

    @GetMapping("/owner")
    Collection<BookingResponseDto> getAllOwnerBookingAtState(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state);
}