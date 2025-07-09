package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
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
    private final ItemService itemService;
    private final ItemRequestMapper itemRequestMapper;
    private final UserMapper userMapper;

    @Override
    public ItemRequestDto createItemRequest(Long userId, ItemRequestDto itemRequestDto) {
        UserDto userDto = userService.getUserById(userId);
        User requestor = userMapper.toUser(userDto);

        ItemRequest itemRequest = itemRequestMapper.toEntity(itemRequestDto);
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest createdRequest = itemRequestStorage.createRequest(itemRequest);
        return enrichWithItems(itemRequestMapper.toDto(createdRequest));
    }

    @Override
    public List<ItemRequestDto> getAllItemRequestsByUser(Long userId) {
        userService.getUserById(userId);

        return itemRequestStorage.getAllByRequestor(userId).stream()
                .map(itemRequestMapper::toDto)
                .map(this::enrichWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Integer from, Integer size) {
        userService.getUserById(userId);

        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Неверные параметры пагинации");
        }

        return itemRequestStorage.getAll(userId, from, size).stream()
                .map(itemRequestMapper::toDto)
                .map(this::enrichWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);

        ItemRequest request = itemRequestStorage.getById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return enrichWithItems(itemRequestMapper.toDto(request));
    }

    private ItemRequestDto enrichWithItems(ItemRequestDto dto) {
        List<ItemDto> items = itemService.findItemsByRequestId(dto.getId());
        dto.setItems(items);
        return dto;
    }
}