package ru.yandex.practicum.filmorate.storage.film;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.util.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.util.FilmValidation;

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
        FilmValidation.validate(film);
        film.setId(generateId());
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!exists(film)) throw new NotFoundException("Неверно указан id фильма");
        FilmValidation.validate(film);
        films.put(film.getId(), film);
        log.info("Обновлен фильм с id = {}", film.getId());
        return film;
    }

    @Override
    public int delete(long id) {
        if (!exists(films.get(id))) throw new NotFoundException("Неверно указан id фильма");
        films.remove(id);
        log.info("Удален фильм с id = {}", id);
        return 1;
    }

    private boolean exists(Film film) {
        if (film.getId() == null || !films.containsKey(film.getId())) {
            log.error("Несуществующий id = {}", film.getId());
            return false;
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

    private long generateId() {
        return films.values().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }

    @Override
    public void like(long id, long userId) {
        Film film = findById(id);
        film.getLikes().add(userId);
        log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, id);
    }

    @Override
    public void deleteLike(long id, long userId) {
        Film film = findById(id);
        film.getLikes().remove(userId);
        log.info("Пользователь с id = {} убрал лайк фильму с id = {}", userId, id);
    }
}
