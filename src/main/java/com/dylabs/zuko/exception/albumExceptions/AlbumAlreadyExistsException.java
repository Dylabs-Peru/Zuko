package com.dylabs.zuko.exception.albumExceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AlbumAlreadyExistsException extends RuntimeException {
    public AlbumAlreadyExistsException(String message) {
        super(message);
    }
}
