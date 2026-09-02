package br.com.charifa.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminReservationResponse(UUID id, String participantName, String phone, String paymentMethod,
        String status, List<Integer> numbers, BigDecimal total, Instant createdAt, Instant expiresAt,
        Instant paymentReportedAt) {
    public static AdminReservationResponse from(Reservation reservation, List<Integer> numbers, Instant now) {
        String effectiveStatus = reservation.getStatus() == ReservationStatus.PENDING
                && !reservation.getExpiresAt().isAfter(now) ? "EXPIRED" : reservation.getStatus().name();
        BigDecimal total = reservation.getRaffle().getNumberPrice().multiply(BigDecimal.valueOf(numbers.size()));
        return new AdminReservationResponse(reservation.getId(), reservation.getParticipantName(), reservation.getPhone(),
                reservation.getPaymentMethod().name(), effectiveStatus, numbers, total,
                reservation.getCreatedAt(), reservation.getExpiresAt(), reservation.getPaymentReportedAt());
    }
}
