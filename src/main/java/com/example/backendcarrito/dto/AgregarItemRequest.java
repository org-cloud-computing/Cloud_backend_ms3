package com.example.backendcarrito.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record AgregarItemRequest(
    @NotNull @Positive Long idProducto,
    @NotBlank String nombre,
    @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2) BigDecimal precioUnitario,
    @org.hibernate.validator.constraints.URL String urlImagen,
    @org.hibernate.validator.constraints.URL String urlProducto,
    @NotNull @Positive Integer cantidad,
    String moneda
) {
    // Constructor de compatibilidad para código existente que pasa 6 argumentos sin moneda
    public AgregarItemRequest(Long idProducto, String nombre, BigDecimal precioUnitario,
                              String urlImagen, String urlProducto, Integer cantidad) {
        this(idProducto, nombre, precioUnitario, urlImagen, urlProducto, cantidad, null);
    }
}
