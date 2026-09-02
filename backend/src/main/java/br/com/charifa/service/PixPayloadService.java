package br.com.charifa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PixPayloadService {
    private static final String GUI = "br.gov.bcb.pix";

    private final String key;
    private final String receiverName;
    private final String receiverCity;

    public PixPayloadService(@Value("${app.pix.key:}") String key,
            @Value("${app.pix.receiver-name:}") String receiverName,
            @Value("${app.pix.receiver-city:}") String receiverCity) {
        this.key = key.trim();
        this.receiverName = normalize(receiverName, 25);
        this.receiverCity = normalize(receiverCity, 15);
    }

    public String create(UUID reservationId, BigDecimal amount) {
        if (key.isBlank() || receiverName.isBlank() || receiverCity.isBlank()) {
            throw new IllegalStateException("Configure PIX_KEY, PIX_RECEIVER_NAME e PIX_RECEIVER_CITY.");
        }
        String merchantAccount = field("00", GUI) + field("01", key);
        String txid = reservationId.toString().replace("-", "").substring(0, 25).toUpperCase(Locale.ROOT);
        String additionalData = field("05", txid);
        String payload = field("00", "01")
                + field("01", "11")
                + field("26", merchantAccount)
                + field("52", "0000")
                + field("53", "986")
                + field("54", amount.setScale(2, RoundingMode.HALF_UP).toPlainString())
                + field("58", "BR")
                + field("59", receiverName)
                + field("60", receiverCity)
                + field("62", additionalData)
                + "6304";
        return payload + crc16(payload);
    }

    private static String field(String id, String value) {
        int size = value.getBytes(StandardCharsets.UTF_8).length;
        return id + String.format(Locale.ROOT, "%02d", size) + value;
    }

    private static String normalize(String value, int maxLength) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9 $%*+./:-]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
        return normalized.substring(0, Math.min(normalized.length(), maxLength));
    }

    private static String crc16(String value) {
        int crc = 0xFFFF;
        for (byte current : value.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (current & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format(Locale.ROOT, "%04X", crc);
    }
}
