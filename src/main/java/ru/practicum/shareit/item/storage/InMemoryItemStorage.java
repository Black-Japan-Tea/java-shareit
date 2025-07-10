package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item createItem(Item item) {
        item.setId(idGenerator.getAndIncrement());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        if (item.getAvailable() == null) {
            item.setAvailable(false);
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getItemById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> getAllItemsByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    private boolean containsText(Item item, String text) {
        return item.getName().toLowerCase().contains(text) ||
               item.getDescription().toLowerCase().contains(text);
    }

    @Override
    public List<Item> searchAvailableItems(String text) {
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> containsText(item, text.toLowerCase()))
                .collect(Collectors.toList());
    }
}