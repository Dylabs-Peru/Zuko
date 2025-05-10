package com.dylabs.zuko.exception;

public class SongAlreadyExistException extends RuntimeException {
    public SongAlreadyExistException(String message) {
        super(message);
    }
}
