package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @Valid @RequestBody ItemDto itemDto) {
        ItemDto createdItem = itemService.createItem(userId, itemDto);
        return ResponseEntity.ok(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItemsByOwner(
            @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.getAllItemsByOwner(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam String text,
            @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> addComment(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        CommentResponseDto comment = itemService.addComment(userId, itemId, commentRequestDto);
        return ResponseEntity.ok(comment);
    }
}