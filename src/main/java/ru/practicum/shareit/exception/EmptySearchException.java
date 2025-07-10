package ru.practicum.shareit.exception;

public class EmptySearchException extends RuntimeException {
    public EmptySearchException(String message) {
        super(message);
    }
}
