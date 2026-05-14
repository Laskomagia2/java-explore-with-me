package ru.yandex.practicum.exception;

public class InvalidRequestParams extends RuntimeException {
    public InvalidRequestParams(String message) {
        super(message);
    }
}
