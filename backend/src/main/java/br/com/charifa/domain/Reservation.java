package br.com.charifa.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raffle_id", nullable = false)
    private Raffle raffle;
    @Column(name = "participant_name", nullable = false) private String participantName;
    @Column(nullable = false) private String phone;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private ReservationStatus status;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected Reservation() {}

    public Reservation(Raffle raffle, String participantName, String phone, PaymentMethod paymentMethod, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.raffle = raffle;
        this.participantName = participantName;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.status = ReservationStatus.PENDING;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Raffle getRaffle() { return raffle; }
    public String getParticipantName() { return participantName; }
    public String getPhone() { return phone; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public Instant getExpiresAt() { return expiresAt; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public void expire() { this.status = ReservationStatus.EXPIRED; }
    public void confirm() { this.status = ReservationStatus.CONFIRMED; }
    public void cancel() { this.status = ReservationStatus.CANCELLED; }
}
