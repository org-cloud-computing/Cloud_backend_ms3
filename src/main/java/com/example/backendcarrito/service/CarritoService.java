package com.example.backendcarrito.service;

import com.example.backendcarrito.dto.*;
import com.example.backendcarrito.exception.RecursoNoEncontradoException;
import com.example.backendcarrito.model.*;
import com.example.backendcarrito.repository.CarritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${api.node.url:http://localhost:3000}")
    private String nodeApiUrl;

    @Value("${api.python.url:http://localhost:8000}")
    private String pythonApiUrl;

    private static final String MONEDA_POR_DEFECTO = "USD";

    private static final Map TASAS_RESPECTO_USD = Map.of(
            "USD", BigDecimal.ONE,
            "PEN", new BigDecimal("3.75"),
            "EUR", new BigDecimal("0.92")
    );

    private static final Map PAIS_A_MONEDA = Map.of(
            "PERÚ", "PEN",
            "PERU", "PEN",
            "USA", "USD",
            "MÉXICO", "USD",
            "COLOMBIA", "USD"
    );

    public String obtenerPaisCliente(String clienteId) {
        try {
            String url = nodeApiUrl + "/clientes/" + clienteId + "/pais";
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("pais")) {
                return String.valueOf(response.get("pais"));
            }
        } catch (Exception e) {
            System.err.println("Error al consultar la API externa de clientes: " + e.getMessage());
        }
        return "Perú";
    }

    public Carrito crearCarrito(CrearCarritoRequest request) {
        if (repository.findByIdClienteAndEstado(request.idCliente(), EstadoCarrito.ACTIVO).isPresent()) {
            throw new IllegalArgumentException("El cliente ya tiene un carrito activo");
        }
        Carrito carrito = new Carrito();
        carrito.setIdCliente(request.idCliente());
        if (request.idAlmacen() != null) {
            carrito.setIdAlmacen(request.idAlmacen());
        }
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setItems(new ArrayList<>());
        carrito.setFechaCreacion(LocalDateTime.now());

        String monedaInicial;
        if (request.moneda() != null && !request.moneda().isBlank()) {
            monedaInicial = normalizarMoneda(request.moneda());
        } else {
            String pais = obtenerPaisCliente(request.idCliente());
            String monedaSugerida = (String) PAIS_A_MONEDA.getOrDefault(pais.toUpperCase(), MONEDA_POR_DEFECTO);
            monedaInicial = normalizarMoneda(monedaSugerida);
        }

        ResumenCarrito resumenInicial = new ResumenCarrito();
        resumenInicial.setMoneda(monedaInicial);
        carrito.setResumen(resumenInicial);

        recalcular(carrito);
        carrito.setFechaActualizacion(carrito.getFechaCreacion());
        return repository.save(carrito);
    }

    public Carrito obtenerPorId(String id) {
        return repository.findById(id).orElseThrow(() ->
                new RecursoNoEncontradoException("Carrito no encontrado: " + id));
    }

    public Carrito obtenerCarritoActivoPorCliente(String clienteId) {
        return repository.findByIdClienteAndEstado(clienteId, EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe carrito activo para el cliente: " + clienteId));
    }

    public Carrito agregarItem(String id, AgregarItemRequest request) {
        Carrito carrito = obtenerPorId(id);
        ItemCarrito existente = carrito.getItems().stream()
                .filter(item -> item.getIdProducto().equals(request.idProducto()))
                .findFirst().orElse(null);
        if (existente == null) {
            carrito.getItems().add(crearItem(request, obtenerMonedaCarrito(carrito)));
        } else {
            existente.setCantidad(sumarCantidades(existente.getCantidad(), request.cantidad()));
        }
        return guardarConResumen(carrito);
    }

    private ItemCarrito crearItem(AgregarItemRequest request, String monedaCarrito) {
        ItemCarrito item = new ItemCarrito();
        item.setIdProducto(request.idProducto());
        item.setNombre(request.nombre());

        BigDecimal precioFinal = request.precioUnitario();
        if (request.moneda() != null && !request.moneda().isBlank()) {
            String monedaItem = normalizarMoneda(request.moneda());
            if (!monedaItem.equalsIgnoreCase(monedaCarrito)) {
                precioFinal = convertir(request.precioUnitario(), monedaItem, monedaCarrito);
            }
        }

        item.setPrecioUnitario(precioFinal.setScale(2, RoundingMode.HALF_UP));
        item.setUrlImagen(request.urlImagen());
        item.setUrlProducto(request.urlProducto());
        item.setCantidad(request.cantidad());
        item.setFechaAgregado(LocalDateTime.now());
        return item;
    }

    public Carrito actualizarCantidadItem(String id, Long productoId, ActualizarCantidadRequest request) {
        Carrito carrito = obtenerPorId(id);
        buscarItem(carrito, productoId).setCantidad(request.cantidad());
        return guardarConResumen(carrito);
    }

    public Carrito eliminarItem(String id, Long productoId) {
        Carrito carrito = obtenerPorId(id);
        carrito.getItems().remove(buscarItem(carrito, productoId));
        return guardarConResumen(carrito);
    }

    public Carrito vaciarCarrito(String id) {
        Carrito carrito = obtenerPorId(id);
        carrito.getItems().clear();
        return guardarConResumen(carrito);
    }

    public Carrito cambiarEstado(String id, CambiarEstadoRequest request) {
        Carrito carrito = obtenerPorId(id);
        if (request.estado() == EstadoCarrito.ACTIVO) {
            repository.findByIdClienteAndEstado(carrito.getIdCliente(), EstadoCarrito.ACTIVO)
                    .filter(activo -> !activo.getId().equals(id))
                    .ifPresent(activo -> { throw new IllegalArgumentException("El cliente ya tiene un carrito activo"); });
        }
        carrito.setEstado(request.estado());
        carrito.setFechaActualizacion(LocalDateTime.now());
        return repository.save(carrito);
    }

    public Carrito cambiarMoneda(String id, String nuevaMoneda) {
        Carrito carrito = obtenerPorId(id);
        String destino = normalizarMoneda(nuevaMoneda);
        String origen = obtenerMonedaCarrito(carrito);

        if (!origen.equalsIgnoreCase(destino)) {
            for (ItemCarrito item : carrito.getItems()) {
                if (item.getPrecioUnitario() != null) {
                    item.setPrecioUnitario(convertir(item.getPrecioUnitario(), origen, destino));
                }
            }
            if (carrito.getResumen() == null) {
                carrito.setResumen(new ResumenCarrito());
            }
            carrito.getResumen().setMoneda(destino);
        }
        return guardarConResumen(carrito);
    }

    public Carrito actualizarContenedor(String id, Integer idContenedor) {
        Carrito carrito = obtenerPorId(id);
        carrito.setIdAlmacen(idContenedor);
        carrito.setFechaActualizacion(LocalDateTime.now());
        return repository.save(carrito);
    }

    public void eliminarCarrito(String id) {
        repository.delete(obtenerPorId(id));
    }

    private ItemCarrito buscarItem(Carrito carrito, Long productoId) {
        return carrito.getItems().stream().filter(item -> item.getIdProducto().equals(productoId))
                .findFirst().orElseThrow(() -> new RecursoNoEncontradoException(
                        "Producto no encontrado en el carrito: " + productoId));
    }

    private Carrito guardarConResumen(Carrito carrito) {
        recalcular(carrito);
        return repository.save(carrito);
    }

    private int sumarCantidades(int primera, int segunda) {
        try {
            return Math.addExact(primera, segunda);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("La cantidad total excede el máximo permitido");
        }
    }

    public String obtenerMonedaCarrito(Carrito carrito) {
        if (carrito.getResumen() != null && carrito.getResumen().getMoneda() != null && !carrito.getResumen().getMoneda().isBlank()) {
            return carrito.getResumen().getMoneda().toUpperCase();
        }
        return MONEDA_POR_DEFECTO;
    }

    public String normalizarMoneda(String moneda) {
        if (moneda == null || moneda.isBlank()) {
            return MONEDA_POR_DEFECTO;
        }
        String codigo = moneda.trim().toUpperCase();
        if (!TASAS_RESPECTO_USD.containsKey(codigo)) {
            throw new IllegalArgumentException("Moneda no soportada: " + codigo + ". Monedas soportadas: " + TASAS_RESPECTO_USD.keySet());
        }
        return codigo;
    }

    public BigDecimal convertir(BigDecimal monto, String origen, String destino) {
        if (monto == null) return BigDecimal.ZERO;
        if (origen.equalsIgnoreCase(destino)) {
            return monto.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal tasaOrigen = (BigDecimal) TASAS_RESPECTO_USD.get(origen);
        BigDecimal tasaDestino = (BigDecimal) TASAS_RESPECTO_USD.get(destino);
        if (tasaOrigen == null || tasaDestino == null) {
            throw new IllegalArgumentException("Conversión no disponible entre " + origen + " y " + destino);
        }
        BigDecimal enUsd = monto.divide(tasaOrigen, 6, RoundingMode.HALF_UP);
        return enUsd.multiply(tasaDestino).setScale(2, RoundingMode.HALF_UP);
    }

    private void recalcular(Carrito carrito) {
        int cantidad = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCarrito item : carrito.getItems()) {
            cantidad = sumarCantidades(cantidad, item.getCantidad());
            if (item.getPrecioUnitario() != null) {
                subtotal = subtotal.add(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())));
            }
        }
        String monedaActual = obtenerMonedaCarrito(carrito);

        ResumenCarrito resumen = carrito.getResumen();
        if (resumen == null) {
            resumen = new ResumenCarrito();
        }
        resumen.setTotalArticulos(cantidad);
        resumen.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        resumen.setMoneda(monedaActual);
        carrito.setResumen(resumen);
        carrito.setFechaActualizacion(LocalDateTime.now());
    }
}
