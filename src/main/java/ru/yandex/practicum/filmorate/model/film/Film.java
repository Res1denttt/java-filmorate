package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    @JsonProperty("duration")
    private Integer duration_sec;
    @JsonProperty("mpa")
    private Rating rating;
    private List<Genre> genres = new ArrayList<>();
    private Set<Long> likes = new HashSet<>();
    @Getter
    private static int maxDescriptionLength = 200;
    @Getter
    private static LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);

    public Film(String name, String description, LocalDate releaseDate, int duration_sec) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration_sec = duration_sec;
    }
}
