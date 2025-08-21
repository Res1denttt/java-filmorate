package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    FilmStorage filmStorage;
    UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void like(long id, long userId) {
        Film film = filmStorage.findById(id);
        User user = userStorage.findById(userId);
        filmStorage.exists(film);
        userStorage.exists(user);
        film.getLikes().add(user);
    }

    public void deleteLike(long id, long userId) {
        Film film = filmStorage.findById(id);
        User user = userStorage.findById(userId);
        filmStorage.exists(film);
        userStorage.exists(user);
        film.getLikes().remove(user);
    }

    public Set<Film> getMostPopular(int size) {
        if (size <= 0) {
            log.error("Некорректная длина списка популряных фильмов = {}", size);
            throw new ValidationException("Длина списка популярных фильмов должна быть больше 0");
        }
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> film.getLikes().size()).reversed())
                .limit(size)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film findById(long id) {
        return filmStorage.findById(id);
    }
}
