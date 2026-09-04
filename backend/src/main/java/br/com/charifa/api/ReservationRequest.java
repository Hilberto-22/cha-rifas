package br.com.charifa.api;

import br.com.charifa.domain.PaymentMethod;
import jakarta.validation.constraints.*;
import java.util.List;

public record ReservationRequest(
        @NotBlank @Size(min = 3, max = 80) String name,
        @NotBlank @Pattern(regexp = "[0-9()+\\-\\s]{10,20}") String phone,
        @NotNull PaymentMethod paymentMethod,
        @NotEmpty @Size(max = 20) List<@Min(1) @Max(150) Integer> numbers) {}
