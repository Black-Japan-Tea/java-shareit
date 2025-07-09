package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ItemAccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public Item createItem(Long userId, Item item) {
        if (!userService.getAllUsers().contains(userService.getUserById(userId))) {
            throw new NotFoundException("Пользователь не найден");
        }
        item.setOwner(userService.getUserById(userId));
        return itemStorage.createItem(item);
    }

    @Override
    public Item updateItem(Long userId, Long itemId, Item item) {
        Item existingItem = getItemById(userId, itemId);
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("Только владелец может обновлять");
        }
        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }
        return itemStorage.updateItem(existingItem);
    }

    @Override
    public Item getItemById(Long userId, Long itemId) {
        return itemStorage.getItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }

    @Override
    public List<Item> getAllItemsByOwner(Long userId) {
        userService.getUserById(userId);
        return itemStorage.getAllItemsByOwner(userId);
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return itemStorage.searchItems(text).stream()
                .filter(Item::getAvailable)
                .collect(Collectors.toList());
    }
}