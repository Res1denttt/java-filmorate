package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {
    Collection<User> findAll();

    User create(User user);

    User update(User user);

    User delete(User user);

    boolean exists(User... users);

    User findById(long id);

    Set<User> getCommonFriends(long userId, long otherUserId);

    Set<User> findFriends(Collection<Long> fiendsId);
}
