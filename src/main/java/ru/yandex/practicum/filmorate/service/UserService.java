package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void makeFriends(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        userStorage.exists(user, friend);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь с id = {} теперь дружит с id = {}", userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);
        userStorage.exists(user, friend);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Set<User> getCommonFriends(long userId, long otherUserId) {
        User user = userStorage.findById(userId);
        User otherUser = userStorage.findById(otherUserId);
        userStorage.exists(user, otherUser);
        return user.getFriends().stream()
                .filter(u -> otherUser.getFriends().contains(u))
                .map(userStorage::findById)
                .collect(Collectors.toSet());
    }

    public Set<User> findFriends(long id) {
        User user = userStorage.findById(id);
        userStorage.exists(user);
        return user.getFriends().stream()
                .map(userStorage::findById)
                .collect(Collectors.toSet());
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
        return userStorage.findById(id);
    }
}
