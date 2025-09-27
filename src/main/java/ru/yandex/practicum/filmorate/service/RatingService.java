package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.storage.film.RatingDbStorage;

import java.util.List;

@Service
public class RatingService {
    private final RatingDbStorage storage;

    public RatingService(RatingDbStorage storage) {
        this.storage = storage;
    }

    public List<Rating> findAll() {
        return storage.findAll();
    }

    public Rating findById(int id) {
        return storage.findById(id).orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден"));
    }
}
