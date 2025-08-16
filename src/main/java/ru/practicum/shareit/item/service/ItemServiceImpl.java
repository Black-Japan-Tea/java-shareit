package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.exception.ItemAccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<ItemDto> getAllItemsByOwner(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь " + userId + " не найден");
        }
        List<Item> items = itemRepository.findByOwnerId(userId);
        return itemMapper.toItemDtoList(items);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> items = itemRepository.searchAvailableItems(text.toLowerCase());
        return itemMapper.toItemDtoList(items);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Вы можете оставлять комментарий только к тем вещам, которые бронировали");
        }

        Comment comment = commentMapper.toComment(commentRequestDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentResponseDto(savedComment);
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);
        item.setAvailable(itemDto.getAvailable() != null ? itemDto.getAvailable() : true);

        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ItemAccessDeniedException("Пользователь не является владельцем вещи");
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

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemDto itemDto = itemMapper.toItemDto(item);

        List<CommentResponseDto> comments = commentRepository.findByItemId(itemId)
                .stream()
                .map(commentMapper::toCommentResponseDto)
                .collect(Collectors.toList());
        itemDto.setComments(comments);

        if (item.getOwner().getId().equals(userId)) {
            addBookingInfo(itemDto, itemId);
        }

        return itemDto;
    }

    private void addBookingInfo(ItemDto itemDto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        Optional<Booking> lastBooking = bookingRepository
                .findFirstByItemIdAndEndBeforeOrderByEndDesc(itemId, now);
        lastBooking.ifPresent(booking -> itemDto.setLastBooking(
                BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build()));

        Optional<Booking> nextBooking = bookingRepository
                .findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, now);
        nextBooking.ifPresent(booking -> itemDto.setNextBooking(
                BookingShortDto.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build()));
    }
}