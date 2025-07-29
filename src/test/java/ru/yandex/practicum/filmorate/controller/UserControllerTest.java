package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;

    @BeforeEach
    void beforeEach() {
        controller = new UserController();
    }

    @Test
    void shouldCreateUser() {
        User user = new User("test@example.com", "testlogin", "Test User", LocalDate.of(1990, 1, 1));
        User created = controller.create(user);
        assertNotNull(created);
        assertTrue(created.getId() > 0);
        assertEquals("test@example.com", created.getEmail());
        assertEquals("testlogin", created.getLogin());
        assertEquals("Test User", created.getName());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenCreateUserWithNullEmail() {
        User user = new User(null, "login", "Name", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateUserWithInvalidEmail() {
        User user = new User("invalid-email", "login", "Name", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateUserWithBlankLogin() {
        User user = new User("email@example.com", " ", "Name", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateUserWithLoginWithSpaces() {
        User user = new User("email@example.com", "invalid login", "Name", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals("Логин не может быть пустым и содержать пробелы", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateUserWithFutureBirthday() {
        User user = new User("email@example.com", "login", "Name", LocalDate.now().plusDays(1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(user));
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }

    @Test
    void shouldSetNameToLogin() {
        User user = new User("email@example.com", "login", " ", LocalDate.of(1990, 1, 1));
        User created = controller.create(user);
        assertEquals("login", created.getName());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User("email@example.com", "login", "Name", LocalDate.of(1990, 1, 1));
        User created = controller.create(user);
        User updatedUser = new User("email@example.com", "login", "Updated Name", LocalDate.of(1990, 1, 1));
        updatedUser.setId(created.getId());
        User updated = controller.update(updatedUser);
        assertEquals("Updated Name", updated.getName());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldThrowExceptionUWhenUpdateInvalidUser() {
        User user = new User("email@example.com", "login", "Name", LocalDate.of(1990, 1, 1));
        user.setId(999L);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.update(user));
        assertEquals("Неверно указан id пользователя", ex.getMessage());
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = new User("user1@example.com", "login1", "User One", LocalDate.of(1990, 1, 1));
        controller.create(user1);
        User user2 = new User("user2@example.com", "login2", "User Two", LocalDate.of(1991, 2, 2));
        controller.create(user2);
        Collection<User> allUsers = controller.findAll();
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.containsAll(List.of(user1, user2)));
    }
}
