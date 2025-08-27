package ru.practicum.shareit.server.request.dto;

import lombok.Data;
import ru.practicum.shareit.server.item.dto.ItemInRequestResponseDto;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
public class RequestWithItemsResponseDto {

    private Long id;
    private String description;
    private LocalDateTime created;
    private Collection<ItemInRequestResponseDto> items;
}
