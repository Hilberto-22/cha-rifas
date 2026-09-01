package br.com.charifa.service;

public class RaffleNotFoundException extends RuntimeException {
    public RaffleNotFoundException() { super("Nenhuma rifa ativa foi encontrada."); }
}
