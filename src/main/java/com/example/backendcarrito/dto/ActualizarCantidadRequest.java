package com.example.backendcarrito.dto;

import jakarta.validation.constraints.*;

public record ActualizarCantidadRequest(
    @NotNull @Positive Integer cantidad
) {}
