package ru.yandex.practicum.filmorate.controller.errorHandling;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    String error;
    String description;
}
