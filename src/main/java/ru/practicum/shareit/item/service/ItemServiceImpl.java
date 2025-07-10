package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.exception.ItemAccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        UserDto ownerDto = userService.getUserById(userId);
        User owner = userMapper.toUser(ownerDto);

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item createdItem = itemStorage.createItem(item);
        return itemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        userService.getUserById(userId);

        Item existingItem = itemStorage.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Item " + itemId + " not found"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("User " + userId + " is not the owner of the item " + itemId);
        }

        Item updatedItem = itemMapper.toItem(itemDto);
        updatedItem.setId(itemId);
        updatedItem.setOwner(existingItem.getOwner());

        Item savedItem = itemStorage.updateItem(updatedItem);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        userService.getUserById(userId);

        Item item = itemStorage.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Item " + itemId + " not found"));

        if (!item.getOwner().getId().equals(userId) && !item.getAvailable()) {
            throw new ItemAccessDeniedException("User " + userId + " doesn't have access to this item");
        }

        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long userId) {
        userService.getUserById(userId);

        List<Item> items = itemStorage.getAllItemsByOwner(userId);
        return itemMapper.toItemDtoList(items);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        try {
            if (text == null || text.isBlank()) {
                return Collections.emptyList();
            }

            String searchText = text.trim().toLowerCase();
            List<Item> items = itemStorage.searchAvailableItems(searchText);

            return items.stream()
                    .map(itemMapper::toItemDto)
                    .peek(dto -> dto.setAvailable(true))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Search operation failed",
                    e
            );
        }
    }
}