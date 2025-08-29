package ru.practicum.shareit.server.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.server.user.dto.UserRequestDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRequestDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldFailValidationWhenNameIsNull() {
        UserRequestDto dto = new UserRequestDto();
        dto.setEmail("test@mail.com");
    }

    @Test
    void shouldPassValidationWhenAllFieldsAreValid() {
        UserRequestDto dto = new UserRequestDto();
        dto.setName("John");
        dto.setEmail("john@example.com");

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}