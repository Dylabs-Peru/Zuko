package com.dylabs.zuko.exception.roleExeptions;

public class RoleAlreadyExistesException extends RuntimeException {
    public RoleAlreadyExistesException(String message) {
        super(message);
    }
}
