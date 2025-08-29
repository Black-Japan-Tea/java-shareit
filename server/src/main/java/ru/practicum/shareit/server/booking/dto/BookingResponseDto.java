package ru.practicum.shareit.server.booking.dto;

import lombok.Data;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.dto.ItemResponseDto;
import ru.practicum.shareit.server.user.dto.ShortUserResponseDto;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemResponseDto item;
    private ShortUserResponseDto booker;
    private BookingStatus status;
}
