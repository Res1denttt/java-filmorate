package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void beforeEach() {
        controller = new FilmController(new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()));
    }

    @Test
    void shouldCreateFilm() {
        Film film = new Film("Matrix", "Sci-fi action", LocalDate.of(1999, 3, 31), 136);
        Film created = controller.create(film);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals("Matrix", created.getName());
        assertEquals(136, created.getDuration_sec());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenCreateFilmWithNullName() {
        Film film = new Film(null, "Description", LocalDate.of(2000, 1, 1), 100);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Название не может быть пустым", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateFilmWithBlankName() {
        Film film = new Film("  ", "Description", LocalDate.of(2000, 1, 1), 100);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Название не может быть пустым", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateFilmWithTooLongDescription() {
        Film film = new Film("Film", "a".repeat(201), LocalDate.of(2000, 1, 1), 100);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Максимальная длина описания — " + Film.getMaxDescriptionLength() + " символов", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateFilmWithReleaseDateBefore1895() {
        Film film = new Film("Film", "Desc", LocalDate.of(1895, 12, 27), 100);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Дата релиза — не раньше " + Film.getFirstFilmDate(), ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCreateFilmWithNonPositiveDuration() {
        Film film = new Film("Film", "Desc", LocalDate.of(2000, 1, 1), 0);
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", ex.getMessage());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film("Old Name", "Old Desc", LocalDate.of(2000, 1, 1), 100);
        Film created = controller.create(film);
        Film updated = new Film("New Name", "New Desc", LocalDate.of(2001, 1, 1), 120);
        updated.setId(created.getId());
        Film result = controller.update(updated);
        assertEquals(created.getId(), result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertEquals(120, result.getDuration_sec());
        assertEquals(1, controller.findAll().size());
    }

    @Test
    void shouldThrowExceptionWhenUpdateFilmWithNonExistingId() {
        Film film = new Film("Name", "Desc", LocalDate.of(2000, 1, 1), 100);
        film.setId(999L);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> controller.update(film));
        assertEquals("Неверно указан id фильма", ex.getMessage());
    }

    @Test
    void shouldReturnAllFilms() {
        Film film1 = new Film("Film1", "Desc1", LocalDate.of(2000, 1, 1), 90);
        Film film2 = new Film("Film2", "Desc2", LocalDate.of(2001, 1, 1), 110);
        controller.create(film1);
        controller.create(film2);
        Collection<Film> allFilms = controller.findAll();
        assertEquals(2, allFilms.size());
    }
}
