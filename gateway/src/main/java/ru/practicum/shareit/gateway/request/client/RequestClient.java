package ru.practicum.shareit.gateway.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.dto.RequestRequestDto;
import ru.practicum.shareit.gateway.request.dto.RequestResponseDto;
import ru.practicum.shareit.gateway.request.dto.RequestWithItemsResponseDto;

import java.util.Collection;

@FeignClient(
        url = "${shareit-server.url}",
        name = "request-client",
        path = "/requests")
public interface RequestClient {

    @PostMapping
    RequestResponseDto createRequest(
            @RequestBody
            RequestRequestDto newRequest,
            @RequestHeader("X-Sharer-User-Id")
            Long userId);


    @GetMapping
    Collection<RequestWithItemsResponseDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id")
            Long userId);

    @GetMapping("/all")
    Collection<RequestWithItemsResponseDto> getOtherUsersRequests(
            @RequestHeader("X-Sharer-User-Id")
            Long userId);

    @GetMapping("/{requestId}")
    RequestWithItemsResponseDto getRequestById(
            @PathVariable
            Long requestId);
}