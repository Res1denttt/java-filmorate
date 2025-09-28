package ru.yandex.practicum.filmorate.it;


import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenresDbStorage;
import ru.yandex.practicum.filmorate.storage.film.RatingDbStorage;
import ru.yandex.practicum.filmorate.storage.film.util.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, RatingDbStorage.class, GenresDbStorage.class, FilmRowMapper.class, GenreRowMapper.class,
        RatingRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class FilmDbTest {
    private final FilmStorage storage;

    @Test
    public void testCreate() {
        Film film = getNewFilm();
        Film filmWithId = storage.create(film);

        Film dbFilm = storage.findById(filmWithId.getId()).get();
        storage.setGenres(List.of(dbFilm));
        assertEquals(film.getGenres(), dbFilm.getGenres());
        assertEquals(film.getName(), dbFilm.getName());
        assertEquals(film.getDurationSec(), dbFilm.getDurationSec());
        assertEquals(film.getRating(), dbFilm.getRating());
        assertEquals(film.getReleaseDate(), dbFilm.getReleaseDate());
    }

    @Test
    public void testUpdate() {
        Film film = getNewFilm();
        film.setDurationSec(null);
        film.setName(null);
        film.setId(1L);
        storage.update(film);

        Film dbFilm = storage.findById(1).get();
        storage.setGenres(List.of(dbFilm));
        assertEquals(film.getReleaseDate(), dbFilm.getReleaseDate());
        assertEquals(film.getGenres(), dbFilm.getGenres());
        assertNotEquals(film.getDurationSec(), dbFilm.getDurationSec());
        assertNotEquals(film.getName(), dbFilm.getName());
    }

    @Test
    public void testDelete() {
        assertNotNull(storage.findById(1));
        Film film = new Film();
        film.setId(1L);
        storage.delete(film);
        assertTrue(storage.findById(1).isEmpty());
    }

    @Test
    public void testFindAll() {
        assertEquals(3, storage.findAll().size());
    }


    @Test
    public void testLike() {
        Film film = new Film();
        film.setId(2L);
        User user = new User();
        user.setId(1L);
        storage.like(film, user);
        Film dbfilm = storage.findById(2).get();
        dbfilm.setLikes(storage.getLikes(dbfilm));
        assertTrue(dbfilm.getLikes().contains(1L));
    }

    @Test
    public void failTestLike() {
        Film film = new Film();
        film.setId(2L);
        User user = new User();
        user.setId(1L);
        storage.like(film, user);
        assertThrows(ValidationException.class, () -> storage.like(film, user));
    }

    @Test
    public void deleteLike() {
        Film film = new Film();
        film.setId(1L);
        User user = new User();
        user.setId(1L);
        storage.deleteLike(film, user);
        assertFalse(storage.findById(1).get().getLikes().contains(1L));
    }

    @Test
    public void testGetMostLiked() {
        List<Film> films = new ArrayList<>(storage.getMostLiked(5));
        assertEquals(1, films.get(0).getId());
    }

    private Film getNewFilm() {
        Film film = new Film("Matrix", "Sci-fi action", LocalDate.of(1999, 3, 31),
                136);
        Genre genre1 = new Genre();
        genre1.setId(1);
        Genre genre2 = new Genre();
        genre2.setId(3);
        List<Genre> genres = List.of(genre1, genre2);
        film.setGenres(genres);
        Rating rating = new Rating();
        rating.setId(2);
        film.setRating(rating);
        return film;
    }
}
