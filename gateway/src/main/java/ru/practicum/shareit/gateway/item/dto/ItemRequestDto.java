package ru.practicum.shareit.gateway.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemRequestDto {

    @Size(max = 255)
    @NotBlank
    private String name;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    private Boolean available;

    private Long requestId;

}