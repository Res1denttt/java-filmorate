package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.util.UserStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void makeFriends(long userId, long friendId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));
        userStorage.makeFriends(userId, friendId);
        log.info("Пользователь с id = {} теперь дружит с id = {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));
        userStorage.deleteFriend(userId, friendId);
        log.info("Пользователь с id = {} больше не дружит с id = {}", userId, friendId);

    }

    public Set<User> getCommonFriends(long userId, long otherUserId) {
        return userStorage.getCommonFriends(userId, otherUserId);
    }

    public Set<User> findFriends(long id) {
        userStorage.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        return userStorage.findFriends(id);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User findById(long id) {
        return userStorage.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id = " + id +
                " не найден"));
    }
}
