package com.example.backendcarrito.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarMonedaRequest(
    @NotBlank(message = "La moneda es obligatoria")
    String moneda
) {}
