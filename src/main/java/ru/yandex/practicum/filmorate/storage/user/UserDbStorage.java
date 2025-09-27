package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.user.Friendship;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.BaseRepository;
import ru.yandex.practicum.filmorate.storage.user.util.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.util.UserValidation;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String CREATE_QUERY = "INSERT INTO users (email, login, user_name, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT u.user_id, u.email, u.login, u.user_name, u.birthday " +
            "FROM users u JOIN user_friends uf ON ((uf.user_id = ? AND uf.friend_id = u.user_id) OR " +
            "(uf.friend_id = ? AND uf.user_id = u.user_id)) WHERE uf.initiator_id = ? OR uf.accepted_at IS NOT NULL";
    private static final String CHECK_FRIENDSHIP_STATUS_QUERY = "SELECT accepted_at, requested_at, initiator_id " +
            "FROM user_friends WHERE user_id = ? AND friend_id = ?";
    private static final String CREATE_FRIENDSHIP_QUERY = "INSERT INTO user_friends (user_id, friend_id, requested_at, " +
            "initiator_id) VALUES (?, ?, ?, ?)";
    private static final String ACCEPT_FRIENDSHIP_QUERY = "UPDATE user_friends SET accepted_at = ? WHERE user_id = ? AND " +
            "friend_id = ?";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
    private static final String CHANGE_FRIENDSHIP_INITIATOR_QUERY = "UPDATE user_friends SET accepted_at = null, " +
            "initiator_id = ? WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_ID_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";


    @Override
    public User create(User user) {
        UserValidation.validate(user);
        if (findIdByEmail(user.getEmail()) >= 0)
            throw new ValidationException("Email = " + user.getEmail() + " уже занят");
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rowsAffected = jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_QUERY, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        if (rowsAffected > 0) {
            user.setId(keyHolder.getKeyAs(Long.class));
            return user;
        }
        log.error("Не удалось создать пользователя");
        throw new RuntimeException("Не удалось создать пользователя");
    }

    @Override
    public User update(User user) {
        User oldUser = findOne(FIND_BY_ID_QUERY, user.getId()).orElseThrow(() -> new NotFoundException("Пользователь " +
                "с id = " + user.getId() + " не найден"));
        UserValidation.validateUpdate(user);
        String email = user.getEmail();
        if (email != null) {
            long id = findIdByEmail(email);
            if (id >= 0 && id != user.getId()) {
                throw new ValidationException("Email = " + user.getEmail() + " уже занят");
            }
        } else {
            email = oldUser.getEmail();
        }
        String login = user.getLogin() != null ? user.getLogin() : oldUser.getLogin();
        String name = user.getName() != null ? user.getName() : oldUser.getName();
        LocalDate birthday = user.getBirthday() != null ? user.getBirthday() : oldUser.getBirthday();

        int rowsAffected = jdbc.update(UPDATE_QUERY, email, login, name, birthday, user.getId());
        if (rowsAffected == 0) {
            log.error("Не удалось обновить пользователя с id = {}", user.getId());
            throw new RuntimeException("Не удалось обновить пользователя");
        }
        return findOne(FIND_BY_ID_QUERY, user.getId()).orElseThrow();
    }

    @Override
    public int delete(long id) {
        return jdbc.update(DELETE_QUERY, id);
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<User> findById(long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    private long findIdByEmail(String email) {
        Optional<User> user = findOne(FIND_ID_BY_EMAIL_QUERY, email);
        if (user.isEmpty()) return -1;
        return user.get().getId();
    }

    @Override
    public void makeFriends(long userId, long anotherUserId) {
        if (userId == anotherUserId) throw new ValidationException("Нельзя себя добавить в друзья");
        long minId = Math.min(userId, anotherUserId);
        long maxId = Math.max(userId, anotherUserId);
        if (getFriendship(userId, anotherUserId).isEmpty()) {
            jdbc.update(CREATE_FRIENDSHIP_QUERY, minId, maxId, OffsetDateTime.now(), userId);
            return;
        }
        Friendship friendship = getFriendship(userId, anotherUserId).get();
        if (friendship.getAcceptedAt() != null) throw new ValidationException("Пользователи уже дружат");
        if (friendship.getInitiatorId() == userId) throw new ValidationException("Запрос на дружбу уже направлен");
        jdbc.update(ACCEPT_FRIENDSHIP_QUERY, OffsetDateTime.now(), minId, maxId);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        if (userId == friendId) throw new ValidationException("Нельзя удалить себя из друзей");
        long minId = Math.min(userId, friendId);
        long maxId = Math.max(userId, friendId);
        Friendship friendship = getFriendship(minId, maxId).orElse(null);
        if (friendship == null) return;
        if (friendship.getAcceptedAt() == null && friendship.getInitiatorId() != userId) return;
        if (friendship.getAcceptedAt() == null) {
            jdbc.update(DELETE_FRIEND_QUERY, minId, maxId);
            return;
        }
        jdbc.update(CHANGE_FRIENDSHIP_INITIATOR_QUERY, friendId, minId, maxId);
    }

    @Override
    public Set<User> findFriends(long userId) {
        return new HashSet<>(findMany(FIND_FRIENDS_QUERY, userId, userId, userId));
    }

    @Override
    public Set<User> getCommonFriends(long userId, long anotherUserId) {
        Set<User> userFriends = findFriends(userId);
        Set<User> anotherUserFriends = findFriends(anotherUserId);
        return userFriends.stream()
                .filter(anotherUserFriends::contains)
                .collect(Collectors.toSet());
    }

    private Optional<Friendship> getFriendship(long userId, long anotherUserId) {
        return jdbc.query(CHECK_FRIENDSHIP_STATUS_QUERY, rs -> {
                    if (rs.next()) {
                        Timestamp acceptedTs = rs.getTimestamp("accepted_at");
                        OffsetDateTime acceptedAt = acceptedTs != null ? acceptedTs.toInstant().atOffset(ZoneOffset.UTC) : null;
                        Timestamp requestedTs = rs.getTimestamp("requested_at");
                        OffsetDateTime requestedAt = requestedTs.toInstant().atOffset(ZoneOffset.UTC);
                        long initiatorId = rs.getObject("initiator_id", Long.class);
                        return Optional.of(new Friendship(acceptedAt, requestedAt, initiatorId));
                    }
                    return Optional.empty();
                },
                Math.min(userId, anotherUserId), Math.max(userId, anotherUserId));
    }
}
