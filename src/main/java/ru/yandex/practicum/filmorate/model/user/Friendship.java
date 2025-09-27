package ru.yandex.practicum.filmorate.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public class Friendship {
    private final OffsetDateTime acceptedAt;
    private final OffsetDateTime requestedAt;
    private final Long initiatorId;
}
