package br.com.charifa.api;

import br.com.charifa.domain.Reservation;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(UUID id, List<Integer> numbers, Instant expiresAt, String pixCopyPaste) {
    public static ReservationResponse from(Reservation reservation, List<Integer> numbers, String pixCopyPaste) {
        return new ReservationResponse(reservation.getId(), numbers, reservation.getExpiresAt(), pixCopyPaste);
    }
}
