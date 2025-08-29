package ru.practicum.shareit.gateway.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testItemRequestDto_Serialize() throws IOException {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setName("Super Book");
        dto.setDescription("An amazing book about Java");
        dto.setAvailable(true);
        dto.setRequestId(100L);

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Super Book");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("An amazing book about Java");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(100);
    }

    @Test
    void testItemRequestDto_Deserialize() throws IOException {
        String content = "{ \"name\": \"Table\", \"description\": \"Wooden table\", \"available\": false, \"requestId\": 200 }";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Table");
        assertThat(dto.getDescription()).isEqualTo("Wooden table");
        assertThat(dto.getAvailable()).isFalse();
        assertThat(dto.getRequestId()).isEqualTo(200L);
    }
}