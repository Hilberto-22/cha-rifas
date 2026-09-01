package br.com.charifa.domain;

import java.time.Instant;

public record AdminLoginResponse(String token, String tokenType, Instant expiresAt, String username) {}
