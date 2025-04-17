package com.traderalerting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddFavoriteRequest {
    @NotBlank(message = "Ticker cannot be blank")
    private String ticker;
}