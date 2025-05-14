package com.dylabs.zuko.exception.playlistExceptions;

public class PlaylistAlreadyExistsException extends RuntimeException {
    public PlaylistAlreadyExistsException(String message) {
        super(message);
    }
}