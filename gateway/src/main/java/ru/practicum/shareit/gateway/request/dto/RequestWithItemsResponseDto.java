package ru.practicum.shareit.gateway.request.dto;

import lombok.Data;
import ru.practicum.shareit.gateway.item.dto.ItemInRequestResponseDto;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
public class RequestWithItemsResponseDto {

    private Long id;
    private String description;
    private LocalDateTime created;
    private Collection<ItemInRequestResponseDto> items;
}