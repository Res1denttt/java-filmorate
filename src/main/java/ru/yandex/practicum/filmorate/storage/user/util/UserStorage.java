package ru.yandex.practicum.filmorate.storage.user.util;

import ru.yandex.practicum.filmorate.model.user.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User user);

    int delete(long id);

    Optional<User> findById(long id);

    Set<User> getCommonFriends(long userId, long otherUserId);

    Set<User> findFriends(long userId);

    void makeFriends(long userId, long friendId);

    void deleteFriend(long userId, long friendId);
}
