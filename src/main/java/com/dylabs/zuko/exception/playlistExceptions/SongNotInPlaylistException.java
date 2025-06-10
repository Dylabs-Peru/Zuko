package com.dylabs.zuko.exception.playlistExceptions;

public class SongNotInPlaylistException extends RuntimeException {
    public SongNotInPlaylistException(String message) {
        super(message);
    }
}
