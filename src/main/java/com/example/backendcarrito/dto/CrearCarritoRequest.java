package com.example.backendcarrito.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;

public record CrearCarritoRequest(
    @NotBlank(message = "idCliente es obligatorio")
    String idCliente,

    @JsonAlias({"idContenedor", "id_contenedor", "id_almacen"})
    Integer idAlmacen,

    String moneda
) {
    // Constructor de compatibilidad para llamadas previas con sólo idCliente
    public CrearCarritoRequest(String idCliente) {
        this(idCliente, null, null);
    }

    public Integer getIdContenedor() {
        return idAlmacen;
    }
}
