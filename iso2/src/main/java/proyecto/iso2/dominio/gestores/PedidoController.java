package proyecto.iso2.dominio.gestores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/pedido")
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
    @Autowired
    private CartaMenuDAO cartaMenuDAO;

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
    public String verMenus(Model model, @RequestParam Long restauranteId, HttpSession session) {
        // Obtener el cliente desde la sesión
        Cliente cliente = (Cliente) session.getAttribute("cliente");

        // Verificar si hay cliente
        if (cliente == null) {
            model.addAttribute("error", "Debes iniciar sesión");
            return "redirect:/login"; // Redirigir si no hay cliente en sesión
        }

        // Obtener restaurante y menús
        Optional<Restaurante> restauranteOpt = restauranteDAO.findById(restauranteId);
        if (!restauranteOpt.isPresent()) {
            model.addAttribute("error", "Restaurante no encontrado");
            return "redirect:/"; // Redirigir si no existe el restaurante
        }

        Restaurante restaurante = restauranteOpt.get();
        if (restaurante == null) {
            model.addAttribute("error", "Restaurante no disponible");
            return "redirect:/";
        }
        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);

        // Pasar datos al modelo
        model.addAttribute("cliente", cliente);
        model.addAttribute("restaurante", restaurante);
        model.addAttribute("cartas", cartas);

        return "verMenus"; // Cargar la vista
    }
    @GetMapping("/confirmarPedido")
    public String confirmarPedido(HttpSession session, Model model) {
        // Verificar si el cliente está en sesión
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/login"; // Si no hay cliente, redirigir al login
        }
        System.out.println("Llegamos a GET /confirmarPedido");
        Pedido pedido = (Pedido) session.getAttribute("pedido");
        model.addAttribute("pedido", pedido);

        // Obtener los datos del pedido desde la sesión
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");
        System.out.println("Carrito en sesión antes del GET: " + carrito);
        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío.");
            return "verMenus"; // Regresar a la página de verMenus si no hay items
        }

        // Calcular el total del pedido y los items
        double total = 0;
        List<ItemMenu> itemsPedido = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Optional<ItemMenu> itemOpt = itemMenuDAO.findById(entry.getKey());
            if (itemOpt.isPresent()) {
                ItemMenu item = itemOpt.get();
                total += item.getPrecio() * entry.getValue();
                itemsPedido.add(item);
            }
        }
        System.out.println("Total calculado (GET confirmarPedido): " + total);
        System.out.println("Items en el pedido (GET confirmarPedido): " + itemsPedido);

        model.addAttribute("cliente", cliente);
        //model.addAttribute("direccion", direccion);
        model.addAttribute("itemsPedido", itemsPedido);
        model.addAttribute("total", total);
        //model.addAttribute("metodosPago", MetodoPago.values());

        return "confirmarPedido"; // Mostrar la vista de confirmarPedido
    }

    @PostMapping("/confirmarPedido")
    public String procesarPedido(@RequestParam MetodoPago metodoPago,
                                 @RequestParam String carrito,
                                 HttpSession session, Model model) throws JsonProcessingException {
        // Verificar si el cliente está en sesión
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/login"; // Si no hay cliente, redirigir al login
        }
        System.out.println("En POST /confirmarPedido");
        // Verificar si hay items en el carrito
        ObjectMapper objectMapper = new ObjectMapper();
        Map<Long, Integer> carritoMap = objectMapper.readValue(carrito, new TypeReference<Map<Long, Integer>>(){});
        System.out.println("carritoMap en PostMapping: "+carritoMap);
        if (carritoMap == null || carritoMap.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            return "verMenus"; // Regresar a la página de verMenus si no hay items
        }
        // Guardar el carrito en la sesión para que esté disponible en el GET
        session.setAttribute("carrito", carritoMap);

        // Obtener la dirección seleccionada por el cliente
        /*Optional<Direccion> direccionOpt = direccionDAO.findById(direccionId);
        if (!direccionOpt.isPresent()) {
            model.addAttribute("error", "Dirección no encontrada");
            return "confirmarPedido"; // Redirigir a la página de confirmarPedido con error
        }
        Direccion direccion = direccionOpt.get();*/

        // Crear y guardar el pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setMetodoPago(metodoPago);
        pedido.setEstado(EstadoPedido.PEDIDO); // Estado inicial del pedido
        pedido.setFecha(LocalDateTime.now());

        System.out.println("Carrito en sesión antes del POST: " + carritoMap);
        // Calcular el total del pedido
        double total = 0;
        Restaurante restaurante = null;
        List<ItemMenu> itemsPedido = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : carritoMap.entrySet()) {
            Long itemMenuId = entry.getKey();
            int cantidad = entry.getValue();
            Optional<ItemMenu> itemMenuOpt = itemMenuDAO.findById(itemMenuId);

            if (itemMenuOpt.isPresent()) {
                ItemMenu itemMenu = itemMenuOpt.get();
                total += itemMenu.getPrecio() * cantidad;
                itemsPedido.add(itemMenu);
                // Si aún no hemos asignado un restaurante, tomamos el del primer item
                if (restaurante == null) {
                    restaurante = itemMenu.getCartaMenu().getRestaurante();
                }
            }
        }
        pedido.setRestaurante(restaurante);
        // Agregar datos al modelo
        model.addAttribute("cliente", cliente);
        //model.addAttribute("direccion", direccion);
        model.addAttribute("itemsPedido", itemsPedido);
        model.addAttribute("total", total);
        model.addAttribute("metodosPago", MetodoPago.values());
        // Guardar el pedido en la base de datos
        pedidoDAO.save(pedido);
        session.setAttribute("pedido", pedido); // Guarda el pedido en sesión
        System.out.println("Fin de PostMapping");
        // Eliminar el carrito de la sesión después de confirmar el pedido
        //session.removeAttribute("carrito");

        // Redirigir al GET de confirmarPedido para mostrar el resumen final
        return "redirect:/pedido/confirmarPedido";
    }
    @PostMapping("/eliminar")
    public String eliminarPedido(@RequestParam Long pedidoId, HttpSession session) {
        Optional<Pedido> pedidoOpt = pedidoDAO.findById(pedidoId);
        if (pedidoOpt.isPresent()) {
            pedidoDAO.delete(pedidoOpt.get());
            session.removeAttribute("pedido");
            session.removeAttribute("pedidoId");
            session.removeAttribute("carrito");
        }
        return "redirect:/"; // O donde quieras llevar al usuario
    }
    @PostMapping("/procesarPago")
    public String procesarPago(@RequestParam Long direccionId, @RequestParam double total,
                               @RequestParam String metodoPago, HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/login";
        }

        Optional<Direccion> direccionOpt = direccionDAO.findById(direccionId);
        if (!direccionOpt.isPresent()) {
            model.addAttribute("error", "Dirección no válida");
            return "confirmarPedido";
        }
        Direccion direccion = direccionOpt.get();

        MetodoPago metodoPagoEnum;
        try {
            metodoPagoEnum = MetodoPago.valueOf(metodoPago);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Método de pago inválido");
            return "confirmarPedido";
        }

        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            return "verMenus";
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        //pedido.setDireccion(direccion);
        pedido.setEstado(EstadoPedido.PEDIDO);
        //pedido.setTotal(total);
        pedido.setMetodoPago(metodoPagoEnum);

        List<ItemMenu> itemsPedido = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Long itemMenuId = entry.getKey();
            int cantidad = entry.getValue();
            Optional<ItemMenu> itemMenuOpt = itemMenuDAO.findById(itemMenuId);

            if (itemMenuOpt.isPresent()) {
                ItemMenu itemMenu = itemMenuOpt.get();
                itemsPedido.add(itemMenu);
            }
        }
        pedido.setItems(itemsPedido);
        pedidoDAO.save(pedido);

        // vaciar carrito
        session.removeAttribute("carrito");

        // Redirigir a la página de pago exitoso
        return "redirect:/pagoExitoso";
    }

    @GetMapping("/pago")
    public String mostrarPago() {
        return "pago"; // Mostrar la página "pedido PAGADO"
    }
}
