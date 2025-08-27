package ru.practicum.shareit.gateway.item.dto;

import lombok.Data;

@Data
public class ItemInRequestResponseDto {

    private Long id;
    private String name;
    private Long ownerId;
}