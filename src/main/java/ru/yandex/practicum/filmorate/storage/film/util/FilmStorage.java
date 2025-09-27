package ru.yandex.practicum.filmorate.storage.film.util;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.Collection;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    int delete(long id);

    Film findById(long id);

    Set<Film> getMostLiked(int size);

    public void like(long id, long userId);

    public void deleteLike(long id, long userId);
}
