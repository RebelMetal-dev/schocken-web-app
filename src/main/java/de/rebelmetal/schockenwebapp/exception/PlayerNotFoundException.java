package de.rebelmetal.schockenwebapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to be thrown when a player is not found.
 * The @ResponseStatus annotation tells Spring to automatically return a 404 Not Found HTTP status
 * when this exception is thrown.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(String message) {
        super(message);

    }
}