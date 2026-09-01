package br.com.charifa.domain;

public class InvalidReservationRequestEException extends RuntimeException {
    public InvalidReservationRequestEException(String message) {
        super(message);
    }
}
