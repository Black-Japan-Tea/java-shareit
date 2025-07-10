package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    Item createItem(Item item);

    Item updateItem(Item item);

    Optional<Item> getItemById(Long itemId);

    List<Item> getAllItemsByOwner(Long ownerId);

    List<Item> searchItems(String text);

    List<Item> searchAvailableItems(String text);
}