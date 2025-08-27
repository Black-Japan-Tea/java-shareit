package ru.practicum.shareit.gateway.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingCreateDtoJsonTest {

    @Autowired
    private JacksonTester<BookingCreateDto> json;

    @Test
    void testBookingCreateDto_Serialize() throws IOException {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(100L);
        dto.setStart(LocalDateTime.of(2099, 1, 1, 12, 0));
        dto.setEnd(LocalDateTime.of(2099, 1, 2, 12, 0));

        JsonContent<BookingCreateDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2099-01-01T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2099-01-02T12:00:00");
    }

    @Test
    void testBookingCreateDto_Deserialize() throws IOException {
        String content = "{ \"itemId\": 200, \"start\": \"2030-01-01T10:00:00\", \"end\": \"2030-01-02T10:00:00\" }";

        BookingCreateDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(200L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 1, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2030, 1, 2, 10, 0));
    }
}