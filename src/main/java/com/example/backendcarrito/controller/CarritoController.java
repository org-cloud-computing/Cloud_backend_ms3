package com.example.backendcarrito.controller;

import com.example.backendcarrito.dto.*;
import com.example.backendcarrito.model.Carrito;
import com.example.backendcarrito.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/carritos")
@RequiredArgsConstructor
@Tag(name = "Carritos", description = "Gestión del carrito de compras")
public class CarritoController {
    private final CarritoService service;

    @PostMapping
    @Operation(summary = "Crear un carrito activo vacío")
    public ResponseEntity<Carrito> crear(@Valid @RequestBody CrearCarritoRequest request) {
        Carrito carrito = service.crearCarrito(request);
        return ResponseEntity.created(URI.create("/api/carritos/" + carrito.getId())).body(carrito);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un carrito por ID")
    public Carrito obtener(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Obtener el carrito activo de un cliente")
    public Carrito obtenerActivo(@PathVariable String clienteId) {
        return service.obtenerCarritoActivoPorCliente(clienteId);
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Agregar un producto o sumar su cantidad si ya existe")
    public Carrito agregar(@PathVariable String id, @Valid @RequestBody AgregarItemRequest request) {
        return service.agregarItem(id, request);
    }

    @PatchMapping("/{id}/items/{productoId}")
    @Operation(summary = "Reemplazar la cantidad de un producto")
    public Carrito actualizar(@PathVariable String id, @PathVariable Long productoId,
                             @Valid @RequestBody ActualizarCantidadRequest request) {
        return service.actualizarCantidadItem(id, productoId, request);
    }

    @DeleteMapping("/{id}/items/{productoId}")
    @Operation(summary = "Eliminar un producto del carrito")
    public Carrito eliminarItem(@PathVariable String id, @PathVariable Long productoId) {
        return service.eliminarItem(id, productoId);
    }

    @DeleteMapping("/{id}/items")
    @Operation(summary = "Vaciar el carrito")
    public Carrito vaciar(@PathVariable String id) {
        return service.vaciarCarrito(id);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado del carrito")
    public Carrito cambiarEstado(@PathVariable String id, @Valid @RequestBody CambiarEstadoRequest request) {
        return service.cambiarEstado(id, request);
    }

    @PatchMapping("/{id}/moneda")
    @Operation(summary = "Cambiar y convertir la divisa del carrito")
    public Carrito cambiarMoneda(@PathVariable String id, @Valid @RequestBody CambiarMonedaRequest request) {
        return service.cambiarMoneda(id, request.moneda());
    }

    @PatchMapping("/{id}/contenedor/{idContenedor}")
    @Operation(summary = "Actualizar el id de contenedor/almacén del carrito")
    public Carrito actualizarContenedor(@PathVariable String id, @PathVariable Integer idContenedor) {
        return service.actualizarContenedor(id, idContenedor);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar el carrito completo")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminarCarrito(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
        @Operation(summary = "Health check del servicio")
        public ResponseEntity health() {
            return ResponseEntity.ok("OK");
        }
}
