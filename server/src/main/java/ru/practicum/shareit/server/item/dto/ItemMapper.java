package ru.practicum.shareit.server.item.dto;

import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public class ItemMapper {

    public static Item toItem(ItemRequestDto itemDto) {

        Item item = new Item();

        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());

        if (itemDto.getRequestId() != null) {
            item.setRequest(itemDto.getRequestId());
        }

        return item;
    }

    public static ItemInRequestResponseDto toItemInRequestResponseDto(Item item) {

        ItemInRequestResponseDto dto = new ItemInRequestResponseDto();

        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setOwnerId(item.getOwner());

        return dto;
    }


    public static ItemResponseDto toItemResponseDto(Item item) {

        ItemResponseDto dto = new ItemResponseDto();

        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        return dto;
    }

    public static ItemWithCommentsResponseDto toItemWithCommentsResponseDto(Item item,
                                                                            LocalDateTime lastBookingDate,
                                                                            LocalDateTime nextBookingDate,
                                                                            List<CommentResponseDto> comments) {

        ItemWithCommentsResponseDto dto = new ItemWithCommentsResponseDto();

        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setLastBooking(lastBookingDate);
        dto.setNextBooking(nextBookingDate);
        dto.setComments(comments);

        return dto;
    }


    public static CommentResponseDto toCommentResponseDto(Comment comment, String authorName) {
        CommentResponseDto dto = new CommentResponseDto();

        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreated(comment.getCreated());
        dto.setAuthorName(authorName);

        return dto;
    }
}

