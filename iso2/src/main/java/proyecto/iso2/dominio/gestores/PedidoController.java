package proyecto.iso2.dominio.gestores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

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

    @GetMapping("/verMenus")
    public String verMenus(Model model, @RequestParam Long restauranteId, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            model.addAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }

        Optional<Restaurante> restauranteOpt = restauranteDAO.findById(restauranteId);
        if (!restauranteOpt.isPresent()) {
            model.addAttribute("error", "Restaurante no encontrado");
            return "redirect:/";
        }

        Restaurante restaurante = restauranteOpt.get();
        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);

        model.addAttribute("cliente", cliente);
        model.addAttribute("restaurante", restaurante);
        model.addAttribute("cartas", cartas);

        return "verMenus";
    }

    @GetMapping("/confirmarPedido")
    public String confirmarPedido(HttpSession session, Model model) {
        Cliente clienteSesion = (Cliente) session.getAttribute("cliente");
        if (clienteSesion == null) {
            return "redirect:/login";
        }

        Optional<Cliente> clienteOpt = clienteDAO.findById(clienteSesion.getIdUsuario());
        if (!clienteOpt.isPresent()) {
            model.addAttribute("error", "No se pudo cargar el cliente");
            return "redirect:/login";
        }
        Cliente cliente = clienteOpt.get();

        Pedido pedido = (Pedido) session.getAttribute("pedido");
        if (pedido == null) {
            model.addAttribute("error", "No hay un pedido en curso");
            return "redirect:/verMenus?restauranteId=" + session.getAttribute("restauranteId");
        }

        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            return "redirect:/verMenus?restauranteId=" + pedido.getRestaurante().getIdUsuario();
        }

        Long direccionId = (Long) session.getAttribute("direccionId");
        Optional<Direccion> direccionOpt = direccionId != null ? direccionDAO.findById(direccionId) : Optional.empty();
        if (!direccionOpt.isPresent()) {
            model.addAttribute("error", "No se seleccionó una dirección válida");
            return "redirect:/verMenus?restauranteId=" + pedido.getRestaurante().getIdUsuario();
        }

        String metodoPago = (String) session.getAttribute("metodoPago");
        if (metodoPago == null || metodoPago.isEmpty()) {
            model.addAttribute("error", "No se seleccionó un método de pago");
            return "redirect:/verMenus?restauranteId=" + pedido.getRestaurante().getIdUsuario();
        }

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

        model.addAttribute("pedido", pedido);
        model.addAttribute("cliente", cliente);
        model.addAttribute("itemsPedido", itemsPedido);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", String.format("%.2f", total));
        model.addAttribute("direccion", direccionOpt.get());
        model.addAttribute("metodoPago", metodoPago);

        return "confirmarPedido";
    }

    @PostMapping("/confirmarPedido")
    public String procesarPedido(
            @RequestParam String metodoPago,
            @RequestParam String carrito,
            @RequestParam Long direccionId,
            HttpSession session,
            Model model) throws JsonProcessingException {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            model.addAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<Long, Integer> carritoMap = objectMapper.readValue(carrito, new TypeReference<Map<Long, Integer>>(){});
        if (carritoMap == null || carritoMap.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            return "verMenus";
        }

        Optional<Direccion> direccionOpt = direccionDAO.findById(direccionId);
        if (!direccionOpt.isPresent()) {
            model.addAttribute("error", "Dirección no válida");
            return "verMenus";
        }

        MetodoPago metodoPagoEnum;
        try {
            metodoPagoEnum = MetodoPago.valueOf(metodoPago);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Método de pago no válido");
            return "verMenus";
        }

        session.setAttribute("carrito", carritoMap);
        session.setAttribute("direccionId", direccionId);
        session.setAttribute("metodoPago", metodoPago);

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PEDIDO);
        pedido.setFecha(LocalDateTime.now());
        pedido.setMetodoPago(metodoPagoEnum); // Guardar el metodo de pado en el pedido

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
                if (restaurante == null) {
                    restaurante = itemMenu.getCartaMenu().getRestaurante();
                }
            }
        }

        pedido.setRestaurante(restaurante);
        pedido.setItems(itemsPedido);
        pedido.setTotal(total); // total precio
        pedidoDAO.save(pedido);

        session.setAttribute("pedido", pedido);
        session.setAttribute("restauranteId", restaurante.getIdUsuario());

        return "redirect:/pedido/confirmarPedido";
    }

    @PostMapping("/eliminar")
    public String eliminarPedido(@RequestParam Long pedidoId, HttpSession session) {
        Optional<Pedido> pedidoOpt = pedidoDAO.findById(pedidoId);
        if (pedidoOpt.isPresent()) {
            pedidoDAO.delete(pedidoOpt.get());
            session.removeAttribute("pedido");
            session.removeAttribute("carrito");
            session.removeAttribute("restauranteId");
            session.removeAttribute("direccionId");
            session.removeAttribute("metodoPago");
        }
        return "redirect:/verMenus";
    }
}