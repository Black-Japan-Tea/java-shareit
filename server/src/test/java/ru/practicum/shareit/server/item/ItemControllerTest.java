package ru.practicum.shareit.server.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.item.controller.ItemController;
import ru.practicum.shareit.server.item.dto.*;
import ru.practicum.shareit.server.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper mapper;

    private ItemResponseDto itemResponseDto;
    private ItemWithCommentsResponseDto itemWithComments;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("Book");
        itemResponseDto.setDescription("Interesting book");
        itemResponseDto.setAvailable(true);

        commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setText("Great!");
        commentResponseDto.setAuthorName("Alice");
        commentResponseDto.setCreated(LocalDateTime.now().minusDays(1));

        itemWithComments = new ItemWithCommentsResponseDto();
        itemWithComments.setId(1L);
        itemWithComments.setName("Book");
        itemWithComments.setDescription("Interesting book");
        itemWithComments.setAvailable(true);
        itemWithComments.setLastBooking(LocalDateTime.now().minusHours(2));
        itemWithComments.setNextBooking(LocalDateTime.now().plusHours(3));
        itemWithComments.setComments(List.of(commentResponseDto));
    }

    @Test
    void createItem_ReturnsCreatedItem() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setName("New Book");
        dto.setDescription("Awesome book");
        dto.setAvailable(true);

        when(itemService.createItem(any(ItemRequestDto.class), eq(100L))).thenReturn(itemResponseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(itemResponseDto.getName()));
    }

    @Test
    void updateItem_ReturnsUpdatedItem() throws Exception {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName("Updated Book");

        when(itemService.updateItem(eq(1L), any(ItemUpdateRequestDto.class), eq(100L)))
                .thenReturn(itemResponseDto);

        mvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(itemResponseDto.getName()));
    }

    @Test
    void getItemById_ReturnsItem() throws Exception {
        when(itemService.getItemById(eq(1L), eq(100L))).thenReturn(itemWithComments);

        mvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.comments[0].text").value("Great!"));
    }

    @Test
    void getItemsByUser_ReturnsListOfItems() throws Exception {
        when(itemService.getItemsByUser(eq(100L))).thenReturn(List.of(itemWithComments));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void searchItems_ReturnsMatchingItems() throws Exception {
        when(itemService.searchItems(eq("book"))).thenReturn(List.of(itemResponseDto));

        mvc.perform(get("/items/search")
                        .param("text", "book")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void addComment_ReturnsComment() throws Exception {
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Nice item!");

        when(itemService.addComment(eq(commentDto), eq(1L), eq(200L)))
                .thenReturn(commentResponseDto);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 200L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great!"));
    }
}