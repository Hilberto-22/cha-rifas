package br.com.charifa.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminLoginRequest(@NotBlank @Size(max = 60) String username,
                                @NotBlank @Size(max = 100) String password) {}
