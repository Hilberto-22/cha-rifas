package br.com.charifa.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.charifa.domain.AdminReservationResponse;
import br.com.charifa.domain.ReservationStatus;
import br.com.charifa.repository.RaffleNumberRepository;
import br.com.charifa.repository.ReservationRepository;

@Service
public class AdminService {
    private final ReservationRepository reservations;
    private final RaffleNumberRepository numbers;
    public AdminService(ReservationRepository reservations, RaffleNumberRepository numbers) {
        this.reservations = reservations; this.numbers = numbers;
    }
    @Transactional(readOnly = true)
    public List<AdminReservationResponse> listReservations() {
        Instant now = Instant.now();
        return reservations.findAllByOrderByCreatedAtDesc().stream().map(reservation -> {
            List<Integer> selected = numbers.findByReservationIdOrderByNumber(reservation.getId()).stream()
                    .map(br.com.charifa.domain.RaffleNumber::getNumber).toList();
            return AdminReservationResponse.from(reservation, selected, now);
        }).toList();
    }

    @Transactional
    public void confirm(UUID reservationId) {
        var reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));
        if (reservation.getStatus() != ReservationStatus.PENDING || !reservation.getExpiresAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Somente uma reserva pendente e válida pode ser confirmada.");
        }
        var reservedNumbers = numbers.findByReservationIdForUpdate(reservationId);
        reservation.confirm(); reservedNumbers.forEach(br.com.charifa.domain.RaffleNumber::confirm);
    }

    @Transactional
    public void cancel(UUID reservationId) {
        var reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));
        if (reservation.getStatus() == ReservationStatus.CANCELLED) return;
        var reservedNumbers = numbers.findByReservationIdForUpdate(reservationId);
        reservation.cancel(); reservedNumbers.forEach(br.com.charifa.domain.RaffleNumber::release);
    }
}
