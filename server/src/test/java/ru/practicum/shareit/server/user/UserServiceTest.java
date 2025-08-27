package ru.practicum.shareit.server.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.dto.UserRequestDto;
import ru.practicum.shareit.server.user.dto.UserResponseDto;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.repository.UserRepository;
import ru.practicum.shareit.server.user.service.UserService;
import ru.practicum.shareit.server.user.service.UserServiceImpl;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository mockRepository;
    UserService userService = new UserServiceImpl(mockRepository);

    @Test
    void createUserTest() {

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Test Name");
        requestDto.setEmail("test@test.ru");

        User user = new User();
        user.setId(1L);
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        Mockito
                .when(mockRepository.save(Mockito.any(User.class)))
                .thenReturn(user);

        ReflectionTestUtils.setField(userService, "repository", mockRepository);
        UserResponseDto result = userService.createUser(requestDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void getUserByIdTest() {

        User user = new User();
        user.setId(1L);
        user.setName("Test Name");
        user.setEmail("test@test.ru");

        Mockito
                .when(mockRepository.getUserById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        ReflectionTestUtils.setField(userService, "repository", mockRepository);
        UserResponseDto result = userService.getUserById(1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void updateUserTest() {

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Test Name");
        requestDto.setEmail("test@test.ru");

        User user = new User();
        user.setId(1L);
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        Mockito
                .when(mockRepository.getUserById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockRepository.save(Mockito.any(User.class)))
                .thenReturn(user);

        ReflectionTestUtils.setField(userService, "repository", mockRepository);
        UserResponseDto result = userService.updateUser(requestDto, 1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void userDeleteTest() {
        Long userId = 1L;

        ReflectionTestUtils.setField(userService, "repository", mockRepository);
        userService.deleteUser(userId);

        Mockito
                .verify(mockRepository, Mockito.times(1))
                .deleteById(userId);
    }


    @Test
    void userNotFoundTest() {

        Mockito
                .when(mockRepository.getUserById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        ReflectionTestUtils.setField(userService, "repository", mockRepository);

        Assertions.assertThrows(NotFoundException.class, () -> {
            userService.getUserById(1L);
        });
    }

    @Test
    void existingUserTest() {

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Test Name");
        requestDto.setEmail("test@test.ru");

        User user = new User();
        user.setId(1L);
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        Mockito
                .when(mockRepository.getUserByEmail(requestDto.getEmail()))
                .thenReturn(Optional.of(user));

        ReflectionTestUtils.setField(userService, "repository", mockRepository);

        Assertions.assertThrows(ConflictException.class, () -> {
            userService.createUser(requestDto);
        });

    }

    @Test
    void updateUserNoMailTest() {

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setName("Test Name");

        User user = new User();
        user.setId(1L);
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        Mockito
                .when(mockRepository.getUserById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockRepository.save(Mockito.any(User.class)))
                .thenReturn(user);

        ReflectionTestUtils.setField(userService, "repository", mockRepository);
        UserResponseDto result = userService.updateUser(requestDto, 1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void updateUserNoNameTest() {

        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setEmail("test@test.ru");

        User user = new User();
        user.setId(1L);
        user.setName(requestDto.getName());
        user.setEmail(requestDto.getEmail());

        Mockito
                .when(mockRepository.getUserById(Mockito.anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(mockRepository.save(Mockito.any(User.class)))
                .thenReturn(user);

        ReflectionTestUtils.setField(userService, "repository", mockRepository);
        UserResponseDto result = userService.updateUser(requestDto, 1L);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }
}