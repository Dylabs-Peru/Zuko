package com.dylabs.zuko.exception.albumExceptions;

public class AlbumNotFoundException extends RuntimeException {
  public AlbumNotFoundException(String message) {
    super(message);
  }
}
