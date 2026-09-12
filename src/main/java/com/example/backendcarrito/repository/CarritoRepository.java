package com.example.backendcarrito.repository;

import com.example.backendcarrito.model.Carrito;
import com.example.backendcarrito.model.EstadoCarrito;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CarritoRepository extends MongoRepository<Carrito, String> {
    Optional<Carrito> findByIdClienteAndEstado(String idCliente, EstadoCarrito estado);
}
