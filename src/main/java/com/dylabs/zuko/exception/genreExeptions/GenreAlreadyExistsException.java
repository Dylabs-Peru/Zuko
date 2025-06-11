package com.dylabs.zuko.exception.genreExeptions;

public class GenreAlreadyExistsException extends RuntimeException{
    public GenreAlreadyExistsException(String message){
        super(message);
    }
}
