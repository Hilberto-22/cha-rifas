package br.com.charifa.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.charifa.domain.PaymentMethod;
import jakarta.validation.Validation;

class ReservationRequestValidationTest {

    @Test
    void acceptsRaffleNumber150() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new ReservationRequest(
                    "Pessoa Teste", "98984991288", PaymentMethod.PIX, List.of(150));

            assertThat(factory.getValidator().validate(request)).isEmpty();
        }
    }

    @Test
    void rejectsRaffleNumberAbove150() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new ReservationRequest(
                    "Pessoa Teste", "98984991288", PaymentMethod.PIX, List.of(151));

            assertThat(factory.getValidator().validate(request)).isNotEmpty();
        }
    }
}
