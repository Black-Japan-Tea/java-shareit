package ru.practicum.shareit.request.storage;

import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.Optional;

public interface ItemRequestStorage {
    ItemRequest createRequest(ItemRequest request);

    List<ItemRequest> getAllByRequestor(Long requestorId);

    List<ItemRequest> getAll(Long userId, Integer from, Integer size);

    Optional<ItemRequest> getById(Long requestId);
}