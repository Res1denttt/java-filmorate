package ru.yandex.practicum.filmorate.storage.user.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDate;

@Slf4j
public class UserValidation {

    private UserValidation() {
    }

    public static void validate(User user) {
        validateEmail(user);
        validateLogin(user);
        validateName(user);
        validateBirthday(user);
    }

    public static void validateUpdate(User user) {
        validateEmail(user);
        validateLogin(user);
        validateNameUpdate(user);
        validateBirthday(user);
    }

    private static void validateEmail(User user) {
        String email = user.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) {
            log.error("Некорректная почта: {}", email);
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
    }

    private static void validateLogin(User user) {
        String login = user.getLogin();
        if (login == null || login.isBlank() || login.contains(" ")) {
            log.error("Некорретный логин: {}", login);
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
    }

    private static void validateName(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private static void validateNameUpdate(User user) {
        String name = user.getName();
        if (name == null || name.isBlank()) user.setName(null);
    }

    private static void validateBirthday(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Некорректная дата рождения: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
