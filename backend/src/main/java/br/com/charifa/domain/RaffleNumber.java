package br.com.charifa.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "raffle_numbers", uniqueConstraints = @UniqueConstraint(name = "uk_raffle_number", columnNames = {"raffle_id", "number"}))
public class RaffleNumber {
    
    @Id 
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raffle_id", nullable = false)
    private Raffle raffle;
    
    @Column(nullable = false) private int number;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) 
    private RaffleNumberStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    protected RaffleNumber() {}
    public UUID getId() { return id; }
    public int getNumber() { return number; }

    public RaffleNumberStatus obterStatusAtual(Instant now) {
        if (status == RaffleNumberStatus.RESERVED && reservation != null
                && reservation.getStatus() == ReservationStatus.PENDING
                && !reservation.getExpiresAt().isAfter(now)) {
            return RaffleNumberStatus.AVAILABLE;
        }
        return status;
    }
    public boolean isAvailable(Instant now) { 
        return obterStatusAtual(now) == RaffleNumberStatus.AVAILABLE; 
    }

    public void reserve(Reservation newReservation) {
        if (reservation != null && reservation.getStatus() == ReservationStatus.PENDING) reservation.expire();
        this.reservation = newReservation;
        this.status = RaffleNumberStatus.RESERVED;
    }

    public void liberarNumeroExpirado(Instant now) {
        if (obterStatusAtual(now) == RaffleNumberStatus.AVAILABLE && status == RaffleNumberStatus.RESERVED) {
            if (reservation != null) reservation.expire();
            reservation = null;
            status = RaffleNumberStatus.AVAILABLE;
        }
    }
    public void confirm() { this.status = RaffleNumberStatus.CONFIRMED; }
    public void release() { this.reservation = null; this.status = RaffleNumberStatus.AVAILABLE; }
}
