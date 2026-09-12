package com.example.backendcarrito.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "carritos")
public class Carrito {
    @Id
    private String id;

    @Field("id_cliente")
    private String idCliente;

    @Field("id_almacen")
    @JsonProperty("id_almacen")
    @JsonAlias({"idContenedor", "id_contenedor", "idAlmacen"})
    private Integer idAlmacen;

    private EstadoCarrito estado;
    private List<ItemCarrito> items;
    private ResumenCarrito resumen;

    @Field("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Field("fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Métodos de compatibilidad y conveniencia para "idContenedor"
    public Integer getIdContenedor() {
        return this.idAlmacen;
    }

    public void setIdContenedor(Integer idContenedor) {
        this.idAlmacen = idContenedor;
    }
}
