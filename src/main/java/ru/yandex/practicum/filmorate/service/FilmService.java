package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.util.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.util.UserStorage;

import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
    }

    public void like(long id, long userId) {
        filmStorage.like(id, userId);
    }

    public void deleteLike(long id, long userId) {
        filmStorage.deleteLike(id, userId);
    }

    public Set<Film> getMostPopular(int size) {
        if (size <= 0) {
            log.error("Некорректная длина списка популряных фильмов = {}", size);
            throw new ValidationException("Длина списка популярных фильмов должна быть больше 0");
        }
        return filmStorage.getMostLiked(size);
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
