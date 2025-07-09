package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @Override
    public User getUserById(Long userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public User createUser(User user) {
        if (userStorage.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("Email " + user.getEmail() + " уже используется");
        }
        return userStorage.createUser(user);
    }

    @Override
    public User updateUser(Long userId, Map<String, String> updates) {
        User user = getUserById(userId);

        if (updates.containsKey("email")) {
            String newEmail = updates.get("email");
            if (userStorage.existsByEmail(newEmail) &&
                !user.getEmail().equals(newEmail)) {
                throw new DuplicateEmailException("Email " + newEmail + " уже используется");
            }
            user.setEmail(newEmail);
        }

        if (updates.containsKey("name")) {
            user.setName(updates.get("name"));
        }

        return userStorage.updateUser(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userStorage.deleteUser(userId);
    }
}