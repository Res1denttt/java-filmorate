package ru.yandex.practicum.filmorate.model.film;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Rating {
    private Integer id;
    private String name;
}

