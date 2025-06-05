package com.dylabs.zuko.exception;

import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException;
import com.dylabs.zuko.exception.albumExceptions.AlbumPermissionException;
import com.dylabs.zuko.exception.albumExceptions.AlbumValidationException;
import com.dylabs.zuko.exception.genreExeptions.GenreInUseException;
import com.dylabs.zuko.exception.roleExeptions.*;
import com.dylabs.zuko.exception.songExceptions.SongAlreadyExistException;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.exception.userExeptions.*;
import com.dylabs.zuko.exception.genreExeptions.GenreAlreadyExistsException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.exception.userExeptions.UserAlreadyExistsException;
import com.dylabs.zuko.exception.artistExeptions.ArtistAlreadyExistsException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
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
    @ExceptionHandler(ArtistAlreadyExistsException.class)
    public ProblemDetail handleArtistAlreadyExists(ArtistAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Artist Already Exists");
        problem.setType(URI.create("/errors/artist-already-exists"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(ArtistNotFoundException.class)
    public ProblemDetail handleArtistNotFound(ArtistNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Artist Not Found");
        problem.setType(URI.create("/errors/artist-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(SongAlreadyExistException.class)
    public ProblemDetail handleSongAlreadyExists(SongAlreadyExistException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Canción duplicada");
        problem.setType(URI.create("/errors/song-exists"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(SongNotFoundException.class)
    public ProblemDetail handleSongNotFound(SongNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Canción no encontrada");
        problem.setType(URI.create("/errors/song-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AlbumAlreadyExistsException.class)
    public ProblemDetail handleAlbumAlreadyExists(AlbumAlreadyExistsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Álbum duplicado");
        problem.setType(URI.create("/errors/album-exists"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AlbumNotFoundException.class)
    public ProblemDetail handleAlbumNotFound(AlbumNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Álbum no encontrado");
        problem.setType(URI.create("/errors/album-not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AlbumPermissionException.class)
    public ProblemDetail handleAlbumPermission(AlbumPermissionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Permiso denegado");
        problem.setType(URI.create("/errors/album-permission"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AlbumValidationException.class)
    public ProblemDetail handleAlbumValidation(AlbumValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Error de validación");
        problem.setType(URI.create("/errors/album-validation"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("No tienes permiso para realizar esta acción");
        problem.setType(URI.create("/errors/access-denied"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(GenreInUseException.class)
    public ProblemDetail handleGenreInUse(GenreInUseException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Género en uso");
        problem.setType(URI.create("/errors/genre-in-use"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

}
