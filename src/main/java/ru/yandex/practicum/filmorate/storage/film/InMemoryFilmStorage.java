package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Long, Film> films = new HashMap<>();
    private Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film create(Film film) {
        validate(film);
        film.setId(generateId());
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        exists(film);
        validate(film);
        films.put(film.getId(), film);
        log.info("Обновлен фильм с id = {}", film.getId());
        return film;
    }

    @Override
    public Film delete(Film film) {
        exists(film);
        films.remove(film);
        log.info("Удален фильм с id = {}", film.getId());
        return film;
    }

    @Override
    public boolean exists(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.error("Несуществующий id = {}", film.getId());
            throw new NotFoundException("Неверно указан id фильма");
        }
        return true;
    }

    @Override
    public Film findById(long id) {
        if (id < 1) {
            log.error("Указан id < 1. Id = {}", id);
            throw new ValidationException("Id не может быть < 1");
        }
        Film film = films.get(id);
        if (film == null) {
            log.error("Несуществующий id = {}", id);
            throw new NotFoundException("Фильма с id = " + id + " не существует");
        }
        log.debug("Запрошен фильм с id = {}", id);
        return film;
    }

    @Override
    public Set<Film> getMostLiked(int size) {
        return findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(size)
                .collect(Collectors.toCollection(LinkedHashSet::new));
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
