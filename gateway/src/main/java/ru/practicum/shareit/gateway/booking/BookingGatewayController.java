package ru.practicum.shareit.gateway.booking;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.client.BookingClient;
import ru.practicum.shareit.gateway.booking.dto.BookingCreateDto;
import ru.practicum.shareit.gateway.booking.dto.BookingResponseDto;
import ru.practicum.shareit.gateway.exception.ValidationException;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class BookingGatewayController {

    private final BookingClient client;

    @PostMapping
    BookingResponseDto createBooking(@RequestBody
                                     @Valid
                                     BookingCreateDto newBookingData,
                                     @RequestHeader("X-Sharer-User-Id")
                                     Long userId) {

        if (newBookingData.getEnd().equals(newBookingData.getStart()) ||
            newBookingData.getEnd().isBefore(newBookingData.getStart())) {
            throw new ValidationException("End date must be after start date");
        }


        return client.createBooking(newBookingData, userId);
    }


    @PatchMapping("{bookingId}")
    BookingResponseDto updateBooking(@RequestHeader("X-Sharer-User-Id")
                                     Long userId,
                                     @PathVariable Long bookingId,
                                     @RequestParam Boolean approved) {
        return client.updateBooking(userId, bookingId, approved);
    }


    @GetMapping("{bookingId}")
    BookingResponseDto getBookingById(@RequestHeader("X-Sharer-User-Id")
                                      Long userId,
                                      @PathVariable Long bookingId) {
        return client.getBookingById(userId, bookingId);
    }

    @GetMapping
    Collection<BookingResponseDto> getAllBookingAtState(
            @RequestHeader("X-Sharer-User-Id")
            Long userId,
            @RequestParam(defaultValue = "ALL")
            String state
    ) {
        return client.getAllBookingAtState(userId, state);
    }

    @GetMapping("/owner")
    Collection<BookingResponseDto> getAllOwnerBookingAtState(
            @RequestHeader("X-Sharer-User-Id")
            Long userId,
            @RequestParam(defaultValue = "ALL")
            String state
    ) {
        return client.getAllOwnerBookingAtState(userId, state);
    }
}