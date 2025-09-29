package ru.yandex.practicum.filmorate.storage.user.util;

import ru.yandex.practicum.filmorate.exceptions.FriendshipAlreadyExistsException;
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

    Set<User> getCommonFriends(User user, User anotherUser);

    Set<User> findFriends(User user);

    void makeFriends(User user, User friend) throws FriendshipAlreadyExistsException;

    void deleteFriend(User user, User friend);

    boolean exists(long... id);
}
