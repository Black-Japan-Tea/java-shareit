package ru.practicum.shareit.gateway.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.item.client.ItemClient;
import ru.practicum.shareit.gateway.item.dto.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemGatewayControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper mapper;

    private ItemResponseDto itemResponseDto;
    private ItemWithCommentsResponseDto itemWithComments;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setText("Great!");
        commentResponseDto.setAuthorName("Alice");
        commentResponseDto.setCreated(now.minusDays(1));

        itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(1L);
        itemResponseDto.setName("Book");
        itemResponseDto.setDescription("Interesting book");
        itemResponseDto.setAvailable(true);

        itemWithComments = new ItemWithCommentsResponseDto();
        itemWithComments.setId(1L);
        itemWithComments.setName("Book");
        itemWithComments.setDescription("Interesting book");
        itemWithComments.setAvailable(true);
        itemWithComments.setLastBooking(now.minusHours(2));
        itemWithComments.setNextBooking(now.plusHours(3));
        itemWithComments.setComments(List.of(commentResponseDto));
    }

    @Test
    void createItem_ValidData_ReturnsCreatedItem() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setName("New Book");
        dto.setDescription("Awesome book");
        dto.setAvailable(true);

        when(itemClient.createItem(any(ItemRequestDto.class), eq(100L))).thenReturn(itemResponseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Book"));
    }

    @Test
    void updateItem_ValidData_ReturnsUpdatedItem() throws Exception {
        ItemUpdateRequestDto dto = new ItemUpdateRequestDto();
        dto.setName("Updated Name");

        when(itemClient.updateItem(eq(1L), any(ItemUpdateRequestDto.class), eq(100L)))
                .thenReturn(itemResponseDto);

        mvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Book"));
    }

    @Test
    void getItemById_ValidId_ReturnsItemWithComments() throws Exception {
        when(itemClient.getItem(eq(1L), eq(100L))).thenReturn(itemWithComments);

        mvc.perform(get("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.comments[0].text").value("Great!"));
    }

    @Test
    void getItemsByUser_ReturnsListOfItems() throws Exception {
        when(itemClient.getItemsByUser(eq(100L))).thenReturn(List.of(itemWithComments));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void searchItems_ValidText_ReturnsMatchingItems() throws Exception {
        when(itemClient.searchItems(eq(100L), eq("book")))
                .thenReturn(List.of(itemResponseDto));

        mvc.perform(get("/items/search")
                        .param("text", "book")
                        .header("X-Sharer-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void addComment_ValidData_ReturnsComment() throws Exception {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Nice!");

        when(itemClient.addComment(eq(dto), eq(100L), eq(1L)))
                .thenReturn(commentResponseDto);

        mvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great!"));
    }
}