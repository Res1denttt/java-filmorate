package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.film.util.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.util.FilmValidation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Primary
@Repository
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private final RatingDbStorage ratingDbStorage;
    private final GenresDbStorage genresDbStorage;

    public FilmDbStorage(JdbcTemplate jdbc,
                         RowMapper<Film> mapper,
                         RatingDbStorage ratingDbStorage,
                         GenresDbStorage genresDbStorage) {
        super(jdbc, mapper);
        this.ratingDbStorage = ratingDbStorage;
        this.genresDbStorage = genresDbStorage;
    }

    private static final String FIND_ALL_QUERY = "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration_sec, " +
            "f.rating_id, r.rating_name FROM films f LEFT JOIN ratings r ON f.rating_id = r.rating_id";
    private static final String FIND_BY_ID_QUERY = FIND_ALL_QUERY + " WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String CREATE_QUERY = "INSERT INTO films (film_name, description, release_date, duration_sec, " +
            "rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET film_name = ?, description = ?, release_date = ?, " +
            "duration_sec = ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String GET_UNIQUE_LIKE_QUERY = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_MOST_POPULAR_QUERY = FIND_ALL_QUERY + " LEFT JOIN likes l ON f.film_id = " +
            "l.film_id GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration_sec, f.rating_id " +
            "ORDER BY COUNT(l.film_id) DESC LIMIT ?";
    private static final String GET_LIKES = "SELECT user_id FROM likes WHERE film_id = ?";


    @Override
    public Film create(Film film) {
        FilmValidation.validate(film);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_QUERY, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setObject(4, film.getDurationSec(), Types.INTEGER);
            int ratingId = film.getRating().getId();
            if (!ratingDbStorage.idExists(ratingId)) throw new NotFoundException("Указан несуществующий рейтинг");
            ps.setInt(5, ratingId);
            return ps;
        }, keyHolder);

        if (rowsAffected < 1) {
            log.error("Не удалось создать фильм");
            throw new RuntimeException("Не удалось создать фильм");
        }
        film.setId(keyHolder.getKeyAs(Long.class));
        if (!genresDbStorage.addFilmGenre(film)) {
            log.warn("Не удалось записать жанры к фильму с id = {}", film.getId());
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = findById(film.getId()).orElseThrow(() -> new NotFoundException("Фильма с id = " + film.getId() +
                "не существует"));
        String name = film.getName() == null ? oldFilm.getName() : film.getName();
        String description = film.getDescription() == null ? oldFilm.getDescription() : film.getDescription();
        LocalDate releaseDate = film.getReleaseDate() == null ? oldFilm.getReleaseDate() : film.getReleaseDate();
        int durationSec = film.getDurationSec() == null ? oldFilm.getDurationSec() : film.getDurationSec();
        int ratingId = film.getRating().getId() == null ? oldFilm.getRating().getId() : film.getRating().getId();
        if (!ratingDbStorage.idExists(ratingId)) throw new NotFoundException("Указан несуществующий рейтинг");

        int rowsAffected = jdbc.update(UPDATE_QUERY, name, description, releaseDate, durationSec, ratingId, film.getId());
        if (rowsAffected < 1) throw new RuntimeException("Не удалось обновить фильм с id = " + film.getId());

        if (!genresDbStorage.updateFilmGenre(film))
            log.warn("Не удалось записать жанры к фильму с id = {}", film.getId());
        return film;
    }

    @Override
    public int delete(Film film) {
        return jdbc.update(DELETE_QUERY, film.getId());
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        setGenres(films);
        return films;
    }

    @Override
    public Optional<Film> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void like(Film film, User user) {
        long id = film.getId();
        long userId = user.getId();
        int numberOfLikes = jdbc.queryForObject(GET_UNIQUE_LIKE_QUERY, Integer.class, id, userId);
        if (numberOfLikes != 0)
            throw new ValidationException("Пользователь с id = " + userId + " уже постаил лайк " +
                    "фильму с id = " + id);
        int rowsAffected = jdbc.update(ADD_LIKE_QUERY, id, userId);
        if (rowsAffected < 1) throw new NotFoundException("Не удалось постваить лайк фильму");
    }

    @Override
    public Set<Film> getMostLiked(int size) {
        List<Film> films = jdbc.query(GET_MOST_POPULAR_QUERY, mapper, size);
        setGenres(films);
        return new LinkedHashSet<>(films);
    }

    @Override
    public void deleteLike(Film film, User user) {
        long id = film.getId();
        long userId = user.getId();
        int rowsAffected = jdbc.update(DELETE_LIKE_QUERY, id, userId);
        if (rowsAffected < 1) throw new NotFoundException("Лайк не найден");
    }

    @Override
    public Set<Long> getLikes(Film film) {
        return new HashSet<>(jdbc.queryForList(GET_LIKES, Long.class, film.getId()));
    }

    @Override
    public void setGenres(List<Film> films) {
        for (Film film : films) {
            film.setGenres(new ArrayList<>(genresDbStorage.getFilmGenre(film.getId())));
        }
    }

}
