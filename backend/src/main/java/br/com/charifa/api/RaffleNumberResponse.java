package br.com.charifa.api;

import java.time.Instant;

import br.com.charifa.domain.RaffleNumber;

public record RaffleNumberResponse(int number, String status) {
    public static RaffleNumberResponse from(RaffleNumber number, Instant now) {
        return new RaffleNumberResponse(number.getNumber(), number.obterStatusAtual(now).name().toLowerCase());
    }
}
