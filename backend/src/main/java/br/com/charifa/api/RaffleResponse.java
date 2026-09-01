package br.com.charifa.api;

import br.com.charifa.domain.Raffle;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RaffleResponse(UUID id, String title, Instant drawAt, BigDecimal numberPrice,
                             BigDecimal firstPrize, BigDecimal secondPrize, boolean pixAvailable,
                             String whatsappNumber) {
    public static RaffleResponse from(Raffle raffle, String whatsappNumber) {
        return new RaffleResponse(raffle.getId(), raffle.getTitle(), raffle.getDrawAt(), raffle.getNumberPrice(),
                raffle.getFirstPrize(), raffle.getSecondPrize(), raffle.getPixKey() != null, whatsappNumber);
    }
}
