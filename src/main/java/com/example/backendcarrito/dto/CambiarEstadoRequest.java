package com.example.backendcarrito.dto;

import jakarta.validation.constraints.*;
import com.example.backendcarrito.model.EstadoCarrito;

public record CambiarEstadoRequest(
    @NotNull EstadoCarrito estado
) {}
