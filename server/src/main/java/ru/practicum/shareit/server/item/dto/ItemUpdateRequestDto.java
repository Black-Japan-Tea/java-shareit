package ru.practicum.shareit.server.item.dto;

import lombok.Data;

@Data
public class ItemUpdateRequestDto {

    private String name;
    private String description;
    private Boolean available;
}
