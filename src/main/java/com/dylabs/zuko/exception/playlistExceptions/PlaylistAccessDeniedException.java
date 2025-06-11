package com.dylabs.zuko.exception.playlistExceptions;

public class PlaylistAccessDeniedException extends RuntimeException {
    public PlaylistAccessDeniedException(String message) {
        super(message);
    }
}
