package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        validate(user);
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("Добавлен новый пользователь: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!exists(user)) throw new NotFoundException("Неверно указан id пользователя");
        validate(user);
        users.put(user.getId(), user);
        log.info("Обновлен пользователь с id = {}, теперь это: {}", user.getId(), user);
        return user;
    }

    @Override
    public User delete(User user) {
        if (!exists(user)) throw new NotFoundException("Неверно указан id пользователя");
        users.remove(user);
        log.info("Удален пользователь с id = {}", user.getId());
        return user;
    }

    @Override
    public boolean exists(User... userList) {
        for (User user : userList) {
            if (user.getId() == null || !users.containsKey(user.getId())) {
                log.error("Несуществующий id = {}", user.getId());
                return false;
            }
        }
        return true;
    }

    @Override
    public User findById(long id) {
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
        return user;
    }

    @Override
    public Set<User> getCommonFriends(long userId, long otherUserId) {
        User user = findById(userId);
        User otherUser = findById(otherUserId);
        return user.getFriends().stream()
                .filter(u -> otherUser.getFriends().contains(u))
                .map(this::findById)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<User> findFriends(Collection<Long> friendsId) {
        return friendsId.stream()
                .map(this::findById)
                .collect(Collectors.toSet());
    }

    private void validate(User user) {
        validateEmail(user);
        validateLogin(user);
        validateName(user);
        validateBirthday(user);
    }

    private void validateEmail(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) {
            log.error("Некорректная почта: {}", email);
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
    }

    private void validateLogin(User user) {
        String login = user.getLogin();
        if (login == null || login.isBlank() || login.contains(" ")) {
            log.error("Некорретный логин: {}", login);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }

    private void validateName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void validateBirthday(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private long generateId() {
        return users.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0) + 1;
    }
}
