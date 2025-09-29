package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.film.Rating;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class RatingDbStorage extends BaseRepository<Rating> {

    public RatingDbStorage(JdbcTemplate jdbc, RatingRowMapper mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_RATINGS_QUERY = "SELECT rating_id, rating_name FROM ratings";
    private static final String FIND_RATING_BY_ID_QUERY = FIND_ALL_RATINGS_QUERY + " WHERE rating_id = ?";
    private static final String GET_ALL_RATING_IDS_QUERY = "SELECT rating_id FROM ratings";


    public List<Rating> findAll() {
        return findMany(FIND_ALL_RATINGS_QUERY);
    }

    public Optional<Rating> findById(int id) {
        return findOne(FIND_RATING_BY_ID_QUERY, id);
    }

    public List<Integer> findAllIds() {
        return jdbc.queryForList(GET_ALL_RATING_IDS_QUERY, Integer.class);
    }

    public boolean idExists(int id) {
        return findAllIds().contains(id);
    }
}
