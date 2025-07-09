package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ItemDto createItem(@RequestHeader(USER_ID_HEADER) Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        log.info("Creating new item: {}, userId: {}", itemDto, userId);
        try {
            return ItemMapper.toItemDto(itemService.createItem(userId, ItemMapper.toItem(itemDto)));
        } catch (NotFoundException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("Updating item: {}, userId: {}", itemDto, userId);
        Item itemUpdates = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.updateItem(userId, itemId, itemUpdates));
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(USER_ID_HEADER) Long userId,
                               @PathVariable Long itemId) {
        log.info("Getting item by ID: {}, userId: {}", itemId, userId);
        return ItemMapper.toItemDto(itemService.getItemById(userId, itemId));
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Getting all items by owner: {}", userId);
        return itemService.getAllItemsByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        log.info("Searching for items with text {}", text);
        return itemService.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}