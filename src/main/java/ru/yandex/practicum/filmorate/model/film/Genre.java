package ru.yandex.practicum.filmorate.model.film;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Genre {
    int id;
    String name;
}
