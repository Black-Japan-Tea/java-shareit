package ru.practicum.shareit.gateway.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserRequestDtoJsonTest {

    @Autowired
    private JacksonTester<UserRequestDto> json;

    @Test
    void testUserRequestDto_Serialize() throws IOException {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("Аркадий");
        dto.setEmail("arc.tsar@example.com");

        var jsonContent = json.write(dto);

        assertThat(jsonContent).extractingJsonPathStringValue("$.name").isEqualTo("Аркадий");
        assertThat(jsonContent).extractingJsonPathStringValue("$.email").isEqualTo("arc.tsar@example.com");
    }

    @Test
    void testUserRequestDto_Deserialize() throws IOException {
        String content = "{ \"name\": \"Аркадий\", \"email\": \"arc.tsar@example.com\" }";

        UserRequestDto dto = json.parseObject(content);

        assertThat(dto.getName()).isEqualTo("Аркадий");
        assertThat(dto.getEmail()).isEqualTo("arc.tsar@example.com");
    }
}