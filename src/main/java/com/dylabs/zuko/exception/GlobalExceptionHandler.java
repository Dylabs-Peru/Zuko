package com.dylabs.zuko.exception;

import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.exception.roleExeptions.*;
import com.dylabs.zuko.exception.userExeptions.*;
import com.dylabs.zuko.exception.genreExeptions.GenreAlreadyExistsException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.exception.userExeptions.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Falló la validación");
        problem.setTitle("Error de validación");
        problem.setProperty("timestamp", Instant.now());
        problem.setType(URI.create("/errors/validation"));

        var fieldErrors = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(fe -> fe.getField(), fe -> fe.getDefaultMessage()));
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(GenreAlreadyExistsException.class)
    public ProblemDetail handleGenreExists(GenreAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflicto de Género");
        problem.setType(URI.create("/errors/genre-exists"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(GenreNotFoundException.class)
    public ProblemDetail handleGenreNotFound(GenreNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Género no encontrado");
        problem.setType(URI.create("/errors/genre-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno inesperado");
        problem.setTitle("Internal Server Error");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(RoleAlreadyExistesException.class)
    public ProblemDetail handleRoleAlreadyExists(RoleAlreadyExistesException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Role Already Exists");
        problem.setType(URI.create("/errors/role-already-exists"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ProblemDetail handleRoleNotFound(RoleNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Role Not Found");
        problem.setType(URI.create("/errors/role-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("User Already Exists");
        problem.setType(URI.create("/errors/user-already-exists"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UserNotFoundExeption.class)
    public ProblemDetail handleUserNotFound(UserNotFoundExeption ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("User Not Found");
        problem.setType(URI.create("/errors/user-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(IncorretPasswordExeption.class)
    public ProblemDetail handleIncorretPassword(IncorretPasswordExeption ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Incorrect Password");
        problem.setType(URI.create("/errors/incorrect-password"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

}
