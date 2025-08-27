package ru.practicum.shareit.server.item.dal;

import ru.practicum.shareit.server.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    Optional<Item> getItemById(Long itemId);

    Collection<Item> getAllUserItems(Long userId);

    Item addItem(Item newItem);

    Item updateItem(Item itemDataToUpdate);

    void deleteItem(Long itemId);

    Collection<Item> searchItems(String searchString);
}
