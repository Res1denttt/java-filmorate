package ru.yandex.practicum.filmorate.storage.film.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;

@Slf4j
public class FilmValidation {

    private FilmValidation() {
    }

    public static void validate(Film film) {
        validateName(film);
        validateDescription(film);
        validateReleaseDate(film);
        validateDuration(film);
    }

    private static void validateName(Film film) {
        if (!StringUtils.hasText(film.getName())) {
            log.error("Не указано название фильма");
            throw new ValidationException("Название не может быть пустым");
        }
    }

    private static void validateDescription(Film film) {
        if (film.getDescription().length() > Film.getMaxDescriptionLength()) {
            log.error("Превышены длинна описания: {}", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — " + Film.getMaxDescriptionLength() + " символов");
        }
    }

    private static void validateReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(Film.getFirstFilmDate())) {
            log.error("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше " + Film.getFirstFilmDate());
        }
    }

    private static void validateDuration(Film film) {
        if (film.getDuration_sec() <= 0) {
            log.error("Некорректная продолжительность фильма: {}", film.getDuration_sec());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
