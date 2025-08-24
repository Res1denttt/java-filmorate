package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Film delete(Film film);

    boolean exists(Film film);

    Film findById(long id);

    Set<Film> getMostLiked(int size);
}
