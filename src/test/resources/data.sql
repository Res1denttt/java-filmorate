INSERT INTO users (email, login, user_name, birthday)
VALUES ('alice@example.com', 'alice', 'Alice', '1990-01-01'),
('bob@example.com', 'bob', 'Bob', '1992-02-02'),
('carol@example.com', 'carol', 'Carol', '1988-03-03');

INSERT INTO films (film_name, description, release_date, duration_sec, rating_id)
VALUES ('Film A', 'Description A', '2000-01-01', 120, 1),
('Film B', 'Description B', '2005-05-05', 150, 2),
('Film C', 'Description C', '2010-10-10', 90, 3);

INSERT INTO likes (film_id, user_id)
VALUES (1, 1), (1, 2), (2, 3);

INSERT INTO user_friends (user_id, friend_id, requested_at, accepted_at, initiator_id)
VALUES (1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
(2, 3, CURRENT_TIMESTAMP, NULL, 2);

INSERT INTO film_genres (film_id, genre_id)
VALUES (1, 1),
(1, 2),
(2, 3),
(3, 4),
(3, 5);