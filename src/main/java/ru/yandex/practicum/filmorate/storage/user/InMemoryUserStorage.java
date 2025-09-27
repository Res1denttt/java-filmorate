package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.util.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.util.UserValidation;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        UserValidation.validate(user);
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!exists(user.getId())) throw new NotFoundException("Неверно указан id пользователя");
        UserValidation.validateUpdate(user);
        users.put(user.getId(), user);
        log.info("Обновлен пользователь с id = {}, теперь это: {}", user.getId(), user);
        return user;
    }

    @Override
    public int delete(long id) {
        if (!exists(id)) throw new NotFoundException("Неверно указан id пользователя");
        users.remove(id);
        log.info("Удален пользователь с id = {}", id);
        return 1;
    }

    private boolean exists(long... user_ids) {
        for (long id : user_ids) {
            if (!users.containsKey(id)) {
                log.error("Несуществующий id = {}", id);
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<User> findById(long id) {
        if (id < 1) {
            log.error("Указан id < 1. Id = {}", id);
            throw new ValidationException("Id не может быть < 1");
        }
        User user = users.get(id);
        if (user == null) {
            log.error("Несуществующий id = {}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не существует");
        }
        log.debug("Запрошен пользователь с id = {}", id);
        return Optional.of(user);
    }

    @Override
    public Set<User> getCommonFriends(long userId, long otherUserId) {
//        User user = findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + "не найден"));
//        User otherUser = findById(otherUserId).orElseThrow(() -> new NotFoundException("Пользователь с id " + userId +
//                "не найден"));
//        return user.getFriends().stream()
//                .filter(u -> otherUser.getFriends().contains(u))
//                .map(this::findById)
//                .collect(Collectors.toSet());
        return Set.of();
    }

    //
    @Override
    public Set<User> findFriends(long user_Id) {
//        return friendsId.stream()
//                .map(this::findById)
//                .collect(Collectors.toSet());
        return Set.of();
    }

    private long generateId() {
        return users.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public void makeFriends(long userId, long friendId) {

    }

    @Override
    public void deleteFriend(long userId, long friendId) {

    }
}
