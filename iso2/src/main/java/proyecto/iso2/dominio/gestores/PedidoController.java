package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    /*@GetMapping("/cliente/{clienteId}")
    public List<Pedido> obtenerPedidosCliente(@PathVariable Long clienteId) {
        Cliente cliente = clienteDAO.findById(clienteId).orElse(null);
        if (cliente == null) {
            return (List<Pedido>) ResponseEntity.notFound().build();
        }

        return pedidoDAO.findByCliente(cliente);
    }*/
    @GetMapping("/restaurante/{restauranteId}")
    public List<Pedido> obtenerPedidosRestaurante(@PathVariable Long restauranteId) {
        Restaurante restaurante = restauranteDAO.findById(restauranteId).orElse(null);
        if (restaurante == null) {
            return (List<Pedido>) ResponseEntity.notFound().build();
        }

        return pedidoDAO.findByRestaurante(restaurante);
    }
    @GetMapping("/verMenus")
    public String verMenus(Model model, @AuthenticationPrincipal Cliente cliente) {
        if (cliente != null) {
            // Asegura que las direcciones están cargadas
            List<Direccion> direcciones = cliente.getDirecciones();
            System.out.println("Direcciones del cliente: " + direcciones);
            model.addAttribute("cliente", cliente);
        }
        return "verMenus";
    }

    /*@PostMapping("/pedidos/confirmar")
    @ResponseBody
    public ResponseEntity<String> confirmarPedido(@RequestBody Map<Long, Map<String, Object>> carrito, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");

        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debe iniciar sesión para hacer un pedido.");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PEDIDO);

        List<DetallePedido> detalles = new ArrayList<>();

        for (Map.Entry<Long, Map<String, Object>> entry : carrito.entrySet()) {
            Long itemId = entry.getKey();
            Map<String, Object> datosItem = entry.getValue();

            Integer cantidad = (Integer) datosItem.get("cantidad");
            if (cantidad > 0) {
                ItemMenu item = itemMenuDAO.findById(itemId).orElse(null);
                if (item != null) {
                    DetallePedido detalle = new DetallePedido();
                    detalle.setItem(item);
                    detalle.setCantidad(cantidad);
                    detalle.setPedido(pedido);
                    detalles.add(detalle);
                }
            }
        }

        pedido.setDetalles(detalles);
        pedidoDAO.save(pedido);

        return ResponseEntity.ok("Pedido confirmado");
    }*/
}
