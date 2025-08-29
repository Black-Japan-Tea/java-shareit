package ru.practicum.shareit.gateway.booking.dto;

import lombok.Data;
import ru.practicum.shareit.gateway.item.dto.ItemResponseDto;
import ru.practicum.shareit.gateway.user.dto.ShortUserResponseDto;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemResponseDto item;
    private ShortUserResponseDto booker;
    private BookingStatusDto status;
}
