package br.com.charifa.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "raffles")
public class Raffle {
    @Id private UUID id;
    @Column(nullable = false) private String title;
    @Column(name = "draw_at", nullable = false) private Instant drawAt;
    @Column(name = "number_price", nullable = false) private BigDecimal numberPrice;
    @Column(name = "first_prize", nullable = false) private BigDecimal firstPrize;
    @Column(name = "second_prize", nullable = false) private BigDecimal secondPrize;
    @Column(name = "pix_key") private String pixKey;
    @Column(nullable = false) private boolean active;

    protected Raffle() {}
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public Instant getDrawAt() { return drawAt; }
    public BigDecimal getNumberPrice() { return numberPrice; }
    public BigDecimal getFirstPrize() { return firstPrize; }
    public BigDecimal getSecondPrize() { return secondPrize; }
    public String getPixKey() { return pixKey; }
    public boolean isActive() { return active; }
}
