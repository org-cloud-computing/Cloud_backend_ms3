package com.example.backendcarrito.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
public class ItemCarrito {
    @Field("id_producto")
    private Long idProducto;
    private String nombre;
    @Field(value = "precio_unitario", targetType = FieldType.DECIMAL128)
    private BigDecimal precioUnitario;
    @Field("url_imagen")
    private String urlImagen;
    @Field("url_producto")
    private String urlProducto;
    private Integer cantidad;
    @Field("fecha_agregado")
    private LocalDateTime fechaAgregado;
}
