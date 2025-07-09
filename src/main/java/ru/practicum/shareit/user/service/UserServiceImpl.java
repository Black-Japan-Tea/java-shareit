package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        return userMapper.toUserDtoList(users);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        validateEmailUniqueness(userDto.getEmail());

        User user = userMapper.toUser(userDto);
        User createdUser = userStorage.createUser(user);
        return userMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            validateEmailUniqueness(userDto.getEmail());
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        User updatedUser = userStorage.updateUser(existingUser);
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        userStorage.deleteUser(userId);
    }

    private void validateEmailUniqueness(String email) {
        if (userStorage.existsByEmail(email)) {
            throw new DuplicateEmailException("Email " + email + " already in use");
        }
    }
}