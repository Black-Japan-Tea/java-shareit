package ru.practicum.shareit.server.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.exception.ConflictException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.dto.UserMapper;
import ru.practicum.shareit.server.user.dto.UserRequestDto;
import ru.practicum.shareit.server.user.dto.UserResponseDto;
import ru.practicum.shareit.server.user.model.User;
import ru.practicum.shareit.server.user.repository.UserRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserResponseDto createUser(UserRequestDto newUser) {

        checkEmailForExisting(-1L, newUser.getEmail());

        return UserMapper.toUserResponseDto(
                repository.save(UserMapper.toUser(newUser)));
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = checkAndGetUserById(userId);


        return UserMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto updateUser(UserRequestDto userDataToUpdate, Long userId) {

        User existingUser = checkAndGetUserById(userId);


        if (userDataToUpdate.getName() == null) {
            userDataToUpdate.setName(existingUser.getName());
        }

        if (userDataToUpdate.getEmail() == null) {
            userDataToUpdate.setEmail(existingUser.getEmail());
        } else {
            checkEmailForExisting(userId, userDataToUpdate.getEmail());
        }

        User user = UserMapper.toUser(userDataToUpdate, userId);

        repository.save(user);

        return UserMapper.toUserResponseDto(user);
    }

    @Override
    public void deleteUser(Long userId) {
        repository.deleteById(userId);
    }

    private User checkAndGetUserById(Long id) {

        Optional<User> mayBeUser = repository.getUserById(id);

        if (mayBeUser.isPresent()) {
            return mayBeUser.get();
        } else {
            throw new NotFoundException("User with id=" + id + " not found");
        }
    }

    private void checkEmailForExisting(Long userId, String email) {
        Optional<User> userToCheck = repository.getUserByEmail(email);

        if (userToCheck.isPresent()) {
            if (!userToCheck.get().getId().equals(userId)) {
                throw new ConflictException("UserServiceImpl: " + email + " is already exist");
            }
        }
    }
}