package ru.practicum.shareit.gateway.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.client.RequestClient;
import ru.practicum.shareit.gateway.request.dto.RequestRequestDto;
import ru.practicum.shareit.gateway.request.dto.RequestResponseDto;
import ru.practicum.shareit.gateway.request.dto.RequestWithItemsResponseDto;

import java.util.Collection;


@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class RequestGatewayController {

    private final RequestClient client;

    @PostMapping
    public RequestResponseDto createRequest(
            @RequestBody
            RequestRequestDto newRequest,
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {
        return client.createRequest(newRequest, userId);
    }

    @GetMapping
    public Collection<RequestWithItemsResponseDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {


        return client.getUserRequests(userId);
    }

    @GetMapping("/all")
    public Collection<RequestWithItemsResponseDto> getOtherUsersRequests(
            @RequestHeader("X-Sharer-User-Id")
            Long userId) {

        return client.getOtherUsersRequests(userId);

    }

    @GetMapping("/{requestId}")
    public RequestWithItemsResponseDto getRequestById(
            @PathVariable
            Long requestId) {

        return client.getRequestById(requestId);

    }
}