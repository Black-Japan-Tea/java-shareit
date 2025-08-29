package ru.practicum.shareit.server.request.service;

import ru.practicum.shareit.server.request.dto.RequestRequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;
import ru.practicum.shareit.server.request.dto.RequestWithItemsResponseDto;

import java.util.Collection;


public interface RequestService {

    RequestResponseDto createRequest(RequestRequestDto newRequest, Long creatorId);

    Collection<RequestWithItemsResponseDto> getUserRequests(Long userId);

    Collection<RequestWithItemsResponseDto> getOtherUsersRequests(Long userId);

    RequestWithItemsResponseDto getRequestById(Long requestId);
}
