CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(64),
    user_name VARCHAR(64),
    birthday DATE
    );
CREATE TABLE IF NOT EXISTS ratings (
    rating_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rating_name VARCHAR(32));

CREATE TABLE IF NOT EXISTS films (
    film_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    film_name VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    release_date DATE,
    duration_sec INTEGER,
    rating_id INTEGER REFERENCES ratings(rating_id));

CREATE TABLE IF NOT EXISTS likes (
    film_id BIGINT REFERENCES films(film_id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id));

CREATE TABLE IF NOT EXISTS user_friends (
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    friend_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE,
    initiator_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id));

CREATE TABLE IF NOT EXISTS genres (
    genre_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    genre_name VARCHAR(32));

CREATE TABLE IF NOT EXISTS film_genres (
    film_id BIGINT REFERENCES films(film_id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES genres(genre_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id));

