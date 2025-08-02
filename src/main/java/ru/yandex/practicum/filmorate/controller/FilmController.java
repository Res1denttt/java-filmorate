package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validate(film);
        film.setId(generateId());
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (film.getId() == null || !exists(film)) {
            log.error("Несуществующий id = {}", film.getId());
            throw new ValidationException("Неверно указан id фильма");
        }
        validate(film);
        films.put(film.getId(), film);
        log.info("Обновлен фильм с id = {}", film.getId());
        return film;
    }

    private boolean exists(Film film) {
        return films.containsKey(film.getId());
    }

    private void validate(Film film) {
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
    }

    private void validateName(Film film) {
        if (!StringUtils.hasText(film.getName())) {
            log.error("Не указано название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
    }

    private void validateDescription(Film film) {
        if (film.getDescription().length() > Film.getMaxDescriptionLength()) {
            log.error("Превышены длинна описания: {}", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — " + Film.getMaxDescriptionLength() + " символов");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(Film.getFirstFilmDate())) {
            log.error("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше " + Film.getFirstFilmDate());
        }
    }

    private void validateDuration(Film film) {
        if (film.getDuration() <= 0) {
            log.error("Некорректная продолжительность фильма: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long generateId() {
        return films.values().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }

}
