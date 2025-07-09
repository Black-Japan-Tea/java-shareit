package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage itemRequestStorage;
    private final UserService userService;
    private final ItemStorage itemStorage;

    @Override
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        userService.getUserById(userId);

        ItemRequest itemRequest = ItemRequestMapper.toEntity(itemRequestDto);
        itemRequest.setRequestor(userService.getUserById(userId));
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest createdRequest = itemRequestStorage.createRequest(itemRequest);
        return ItemRequestMapper.toDto(createdRequest);
    }

    @Override
    public List<ItemRequestDto> getAllItemRequestsByUser(Long userId) {
        userService.getUserById(userId);

        List<ItemRequest> requests = itemRequestStorage.getAllByRequestor(userId);
        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = ItemRequestMapper.toDto(request);
                    dto.setItems(getItemsForRequest(request.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);

        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Неверные параметры пагинации");
        }

        List<ItemRequest> requests = itemRequestStorage.getAll(userId, from, size);

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = ItemRequestMapper.toDto(request);
                    dto.setItems(getItemsForRequest(request.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);

        ItemRequest request = itemRequestStorage.getById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        ItemRequestDto dto = ItemRequestMapper.toDto(request);
        dto.setItems(getItemsForRequest(requestId));
        return dto;
    }

    private List<ItemDto> getItemsForRequest(Long requestId) {
        return itemStorage.findByRequestId(requestId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}