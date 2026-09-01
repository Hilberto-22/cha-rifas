package br.com.charifa.domain;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.charifa.service.NumberUnavailableException;
import br.com.charifa.service.RaffleNotFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    record ErrorResponse(String message, List<Integer> unavailable, Instant timestamp) {}

    @ExceptionHandler(NumberUnavailableException.class)
    ResponseEntity<ErrorResponse> conflict(NumberUnavailableException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(exception.getMessage(), exception.getNumbers(), Instant.now()));
    }

    @ExceptionHandler(RaffleNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(RaffleNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(exception.getMessage(), List.of(), Instant.now()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Confira os dados informados e tente novamente.", List.of(), Instant.now()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Usuário ou senha inválidos.", List.of(), Instant.now()));
    }
}
