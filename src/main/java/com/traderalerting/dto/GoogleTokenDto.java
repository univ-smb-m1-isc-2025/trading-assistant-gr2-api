package com.traderalerting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GoogleTokenDto {
    @NotBlank(message = "Google token cannot be blank")
    private String token; // Recevra le credential (ID token) envoyé par le frontend
}
