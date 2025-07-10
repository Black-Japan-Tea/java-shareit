package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        Item existingItem = getExistingItem(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("User " + userId + " is not the owner of the item " + itemId);
        }
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
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
        Item item = getExistingItem(itemId);
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
        if (text == null || text.isBlank() || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchText = text.trim().toLowerCase();
        List<Item> items = itemStorage.searchAvailableItems(searchText);
        return itemMapper.toItemDtoList(items);
    }

    private Item getExistingItem(Long itemId) {
        return itemStorage.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Item " + itemId + " not found"));
    }
}