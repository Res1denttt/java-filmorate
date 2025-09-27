package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.BaseRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenresDbStorage extends BaseRepository<Genre> {

    public GenresDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    private static final String GET_ALL_GENRES_QUERY = "SELECT genre_id, genre_name FROM genres";
    private static final String GET_GENRE_BY_ID_QUERY = GET_ALL_GENRES_QUERY + " WHERE genre_id = ?";
    private static final String GET_ALL_GENRE_IDS_QUERY = "SELECT genre_id FROM genres";
    private static final String GET_GENRES_QUERY = "SELECT fg.genre_id, g.genre_name FROM film_genres fg " +
            "JOIN genres g ON fg.genre_id = g.genre_id WHERE film_id = ? ORDER BY fg.genre_id";
    private static final String ADD_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRE_QUERY = "DELETE FROM film_genres WHERE film_id = ?";

    public List<Genre> findAllGenres() {
        return findMany(GET_ALL_GENRES_QUERY);
    }

    public Optional<Genre> findById(int id) {
        return findOne(GET_GENRE_BY_ID_QUERY, id);
    }

    public List<Integer> findAllIds() {
        return jdbc.queryForList(GET_ALL_GENRE_IDS_QUERY, Integer.class);
    }

    public boolean idExists(int id) {
        return findAllIds().contains(id);
    }

    public List<Genre> getFilmGenre(long filmId) {
        return jdbc.query(GET_GENRES_QUERY, mapper, filmId);
    }

    public void deleteFilmGenre(long filmId) {
        jdbc.update(DELETE_GENRE_QUERY, filmId);
    }

    public boolean addFilmGenre(Film film) {
        Set<Genre> genres = new HashSet<>(film.getGenres());
        if (genres.isEmpty()) return true;
        int rowsAffected = 0;
        for (Genre genre : genres) {
            int genreId = genre.getId();
            if (!idExists(genreId)) throw new NotFoundException("Указан несуществующий жанр");
            rowsAffected += jdbc.update(ADD_GENRE_QUERY, film.getId(), genreId);
        }
        return rowsAffected == genres.size();
    }

    public boolean updateFilmGenre(Film film) {
        if (film.getGenres().isEmpty()) return true;
        deleteFilmGenre(film.getId());
        return addFilmGenre(film);
    }
}
