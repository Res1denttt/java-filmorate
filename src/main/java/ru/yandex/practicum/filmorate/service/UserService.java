package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.FriendshipAlreadyExistsException;
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
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User friend = userStorage.findById(friendId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + friendId + " не найден"));
        try {
            userStorage.makeFriends(user, friend);
        } catch (FriendshipAlreadyExistsException e) {
            log.debug(e.getMessage());
        }
        log.info("Пользователь с id = {} теперь дружит с id = {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User friend = userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));
        userStorage.deleteFriend(user, friend);
        log.info("Пользователь с id = {} больше не дружит с id = {}", userId, friendId);

    }

    public Set<User> getCommonFriends(long userId, long otherUserId) {
        User user = userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId +
                " не найден"));
        User anotherUser = userStorage.findById(otherUserId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId +
                " не найден"));
        return userStorage.getCommonFriends(user, anotherUser);
    }

    public Set<User> findFriends(long id) {
        User user = userStorage.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        return userStorage.findFriends(user);
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
