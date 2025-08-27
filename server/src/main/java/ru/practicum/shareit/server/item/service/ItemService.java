package ru.practicum.shareit.server.item.service;

import ru.practicum.shareit.server.item.dto.*;

import java.util.Collection;

public interface ItemService {

    ItemResponseDto createItem(ItemRequestDto newItem, Long userId);

    ItemResponseDto updateItem(Long itemId, ItemUpdateRequestDto itemDataToUpdate, Long userId);

    ItemWithCommentsResponseDto getItemById(Long itemId, Long userId);

    Collection<ItemWithCommentsResponseDto> getItemsByUser(Long userId);

    Collection<ItemResponseDto> searchItems(String searchString);

    CommentResponseDto addComment(CommentRequestDto dto, Long itemId, Long userId);
}
