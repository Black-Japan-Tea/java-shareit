package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "available", source = "available")
    ItemDto toItemDto(Item item);

    @Mapping(target = "owner", ignore = true)
    Item toItem(ItemDto itemDto);

    List<ItemDto> toItemDtoList(List<Item> items);
}