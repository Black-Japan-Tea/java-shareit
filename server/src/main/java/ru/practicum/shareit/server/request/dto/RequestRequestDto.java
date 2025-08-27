package ru.practicum.shareit.server.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestRequestDto {

    @NotBlank
    private String description;

}