package ru.practicum.shareit.server.item.dto;

import lombok.Data;

@Data
public class ItemInRequestResponseDto {

    private Long id;
    private String name;
    private Long ownerId;
}
