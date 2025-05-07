package com.dylabs.zuko.exception.userExeptions;

public class IncorretPasswordExeption extends RuntimeException {
    public IncorretPasswordExeption(String message) {
        super(message);
    }
}
