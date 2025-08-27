package ru.practicum.shareit.server.request.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.request.dto.RequestRequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;
import ru.practicum.shareit.server.request.dto.RequestWithItemsResponseDto;
import ru.practicum.shareit.server.request.service.RequestService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    public RequestResponseDto createRequest(
            @RequestBody
            RequestRequestDto newRequest,
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {
        return requestService.createRequest(newRequest, userId);
    }


    @GetMapping
    public Collection<RequestWithItemsResponseDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {


        return requestService.getUserRequests(userId);
    }


    @GetMapping("/all")
    public Collection<RequestWithItemsResponseDto> getOtherUsersRequests(
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {

        return requestService.getOtherUsersRequests(userId);

    }

    @GetMapping("/{requestId}")
    public RequestWithItemsResponseDto getRequestById(
            @PathVariable
            Long requestId) {

        return requestService.getRequestById(requestId);

    }
}