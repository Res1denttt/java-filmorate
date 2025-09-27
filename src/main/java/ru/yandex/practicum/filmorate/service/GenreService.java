package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenresDbStorage;

import java.util.List;

@Service
public class GenreService {
    private final GenresDbStorage storage;

    public GenreService(GenresDbStorage storage) {
        this.storage = storage;
    }

    public List<Genre> findAll() {
        return storage.findAllGenres();
    }

    public Genre findById(int id) {
        return storage.findById(id).orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}
