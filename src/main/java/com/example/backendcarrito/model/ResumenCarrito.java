package com.example.backendcarrito.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import java.math.BigDecimal;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
public class ResumenCarrito {
    @Field("total_articulos")
    private Integer totalArticulos;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;
    private String moneda;
}
