package br.com.charifa.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PixPayloadServiceTest {
    @Test
    void createsBrCodePayloadWithAmountAndCrc() {
        var service = new PixPayloadService("12345678900", "José da Silva", "Macapá");

        String payload = service.create(UUID.fromString("9d325c50-d845-4bf3-85e7-1bf14e9ed928"),
                new BigDecimal("50.00"));

        assertTrue(payload.startsWith("000201010211"));
        assertTrue(payload.contains("br.gov.bcb.pix"));
        assertTrue(payload.contains("540550.00"));
        assertTrue(payload.matches(".*6304[0-9A-F]{4}$"));
    }
}
