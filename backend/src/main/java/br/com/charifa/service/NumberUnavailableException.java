package br.com.charifa.service;

import java.util.List;

public class NumberUnavailableException extends RuntimeException {
    private final List<Integer> numbers;
    public NumberUnavailableException(List<Integer> numbers) {
        super("Um ou mais números acabaram de ser escolhidos.");
        this.numbers = List.copyOf(numbers);
    }
    public List<Integer> getNumbers() { return numbers; }
}
