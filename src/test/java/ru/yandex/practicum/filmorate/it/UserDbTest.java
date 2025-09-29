package ru.yandex.practicum.filmorate.it;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exceptions.FriendshipAlreadyExistsException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.util.UserStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserDbTest {
    private final UserStorage storage;

    @Test
    public void testCreate() {
        User user = storage.create(new User("test@example.com", "testlogin", "Test User",
                LocalDate.of(1990, 1, 1)));
        assertThat(storage.findById(user.getId()))
                .isPresent()
                .hasValueSatisfying(dbUser -> {
                    assertEquals(dbUser.getEmail(), user.getEmail());
                    assertEquals(dbUser.getName(), user.getName());
                    assertEquals(dbUser.getLogin(), user.getLogin());
                });
    }

    @Test
    public void testUpdate() {
        System.out.println(storage.findById(1));
        User user = new User("test@example.com", "testlogin", null,
                LocalDate.of(1990, 1, 1));
        user.setId(1L);
        storage.update(user);
        System.out.println(storage.findById(1));
        assertThat(storage.findById(1))
                .isPresent()
                .hasValueSatisfying(dbUser -> {
                    assertEquals(dbUser.getEmail(), user.getEmail());
                    assertNotEquals(dbUser.getName(), user.getName());
                    assertEquals(dbUser.getLogin(), user.getLogin());
                });
    }

    @Test
    public void testDeleteT() {
        assertTrue(storage.findById(1).isPresent());
        storage.delete(1);
        assertTrue(storage.findById(1).isEmpty());
    }


    @Test
    public void testFindAllTest() {
        assertEquals(3, storage.findAll().size());
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = storage.findById(1);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testMakeFriends() throws FriendshipAlreadyExistsException {
        User user = new User();
        user.setId(3L);
        User friend = new User();
        friend.setId(1L);
        storage.makeFriends(user, friend);
        assertTrue(storage.findFriends(user).contains(storage.findById(1).get()));
        assertFalse(storage.findFriends(friend).contains(storage.findById(3).get()));
    }

    @Test
    public void testDeleteFriend() {
        User user = new User();
        user.setId(1L);
        User friend = new User();
        friend.setId(2L);
        assertEquals(1, storage.findFriends(user).size());
        storage.deleteFriend(user, friend);
        assertTrue(storage.findFriends(user).isEmpty());
    }

    @Test
    public void testGetCommonFriends() throws FriendshipAlreadyExistsException {
        User user = new User();
        user.setId(1L);
        User friend = new User();
        friend.setId(3L);
        storage.makeFriends(user, friend);
        User anotherUser = new User();
        anotherUser.setId(2L);
        assertEquals(1, storage.getCommonFriends(user, anotherUser).size());
    }
}
