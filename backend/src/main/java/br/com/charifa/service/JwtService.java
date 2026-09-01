package br.com.charifa.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.charifa.domain.AdminLoginResponse;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final Duration expiration;
    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.encoder = encoder; this.expiration = Duration.ofMinutes(expirationMinutes);
    }
    public AdminLoginResponse issue(String username) {
        Instant now = Instant.now(); Instant expiresAt = now.plus(expiration);
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("cha-rifa-api")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(username)
            .claim("roles", List.of("ADMIN"))
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AdminLoginResponse(token, "Bearer", expiresAt, username);
    }
}
