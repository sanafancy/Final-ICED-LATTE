package proyecto.iso2.dominio.gestores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    @Autowired
    private PedidoDAO pedidoDAO;
    @Autowired
    private ClienteDAO clienteDAO;
    @Autowired
    private RestauranteDAO restauranteDAO;
    @Autowired
    private ItemMenuDAO itemMenuDAO;
    @Autowired
    private DireccionDAO direccionDAO;

    @PostMapping("/crear")
    public Pedido crearPedido(
            @RequestParam Long clienteId,
            @RequestParam Long restauranteId,
            @RequestParam Long direccionId,
            @RequestParam MetodoPago metodoPago,
            @RequestParam List<Long> itemIds) {
        Cliente cliente = clienteDAO.findById(clienteId).orElseThrow();
        Restaurante restaurante = restauranteDAO.findById(restauranteId).orElseThrow();
        Direccion direccion = direccionDAO.findById(direccionId).orElseThrow();

        List<ItemMenu> items = itemMenuDAO.findAllById(itemIds);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setEstado(EstadoPedido.PEDIDO);
        pedido.setMetodoPago(metodoPago);
        pedido.setFecha(LocalDateTime.now());
        pedido.setItems(items);

        return pedidoDAO.save(pedido);
    }
    @GetMapping("/cliente/{clienteId}")
    public List<Pedido> obtenerPedidosCliente(@PathVariable Long clienteId) {
        Cliente cliente = clienteDAO.findById(clienteId).orElse(null);
        if (cliente == null) {
            return (List<Pedido>) ResponseEntity.notFound().build();
        }

        return pedidoDAO.findByCliente(cliente);
    }
    @GetMapping("/restaurante/{restauranteId}")
    public List<Pedido> obtenerPedidosRestaurante(@PathVariable Long restauranteId) {
        Restaurante restaurante = restauranteDAO.findById(restauranteId).orElse(null);
        if (restaurante == null) {
            return (List<Pedido>) ResponseEntity.notFound().build();
        }

        return pedidoDAO.findByRestaurante(restaurante);

    }
}
