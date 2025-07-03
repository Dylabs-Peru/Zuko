package com.dylabs.zuko.exception.songExceptions;

public class SongNotPublicException extends RuntimeException {
    public SongNotPublicException(String message) {
        super(message);
    }
}
