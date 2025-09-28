package ru.yandex.practicum.filmorate.storage.film.util;

import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;

import java.util.*;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    int delete(Film film);

    Optional<Film> findById(long id);

    Set<Film> getMostLiked(int size);

    void like(Film film, User user);

    void deleteLike(Film film, User user);

    Set<Long> getLikes(Film film);

    void setGenres(List<Film> films);
}