package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ServicioEntregaController {
    @Autowired
    private ServicioEntregaDAO servicioEntregaDAO;
    @Autowired
    private PedidoDAO pedidoDAO;
    @Autowired
    private DireccionDAO direccionDAO;
    @Autowired
    private RepartidorDAO repartidorDAO;
    @Autowired
    private PagoDAO pagoDAO;
    @Autowired
    private ItemMenuDAO itemMenuDAO;

    @PostMapping("/pedido/finalizar")
    public String finalizarPedido(
            @RequestParam Long direccionId,
            @RequestParam Long pedidoId,
            @RequestParam String metodoPago,
            HttpSession session,
            Model model) {
        System.out.println("Entrando a método finalizarPedido");

        // Verificar cliente en sesión
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            model.addAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }

        // Obtener pedido y dirección
        Optional<Pedido> pedidoOpt = pedidoDAO.findById(pedidoId);
        Optional<Direccion> direccionOpt = direccionDAO.findById(direccionId);
        if (!pedidoOpt.isPresent() || !direccionOpt.isPresent()) {
            model.addAttribute("error", "Pedido o dirección no válidos");
            return "confirmarPedido";
        }

        Pedido pedido = pedidoOpt.get();
        Direccion direccion = direccionOpt.get();

        // Validar método de pago
        MetodoPago metodoPagoEnum;
        try {
            metodoPagoEnum = MetodoPago.valueOf(metodoPago);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Método de pago no válido");
            return "confirmarPedido";
        }

        // Verificar y actualizar ítems del pedido desde el carrito
        Map<Long, Integer> carrito = (Map<Long, Integer>) session.getAttribute("carrito");
        if (carrito == null || carrito.isEmpty()) {
            model.addAttribute("error", "El carrito está vacío");
            return "confirmarPedido";
        }

        List<ItemMenu> items = new java.util.ArrayList<>();
        for (Long itemId : carrito.keySet()) {
            Optional<ItemMenu> itemOpt = itemMenuDAO.findById(itemId);
            if (itemOpt.isPresent()) {
                items.add(itemOpt.get());
            }
        }
        pedido.setItems(items);
        pedido.setFecha(LocalDateTime.now());

        // Crear y guardar el pago
        Pago pago = new Pago();
        pago.setMetodoPago(metodoPagoEnum);
        pago.setPedido(pedido);
        pagoDAO.save(pago);

        // Actualizar estado del pedido a PAGADO
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setPago(pago);
        pedidoDAO.save(pedido);

        // Asignar repartidor (lógica simple: tomar el primero disponible)
        List<Repartidor> repartidores = repartidorDAO.findAll();
        if (repartidores.isEmpty()) {
            model.addAttribute("error", "No hay repartidores disponibles");
            return "confirmarPedido";
        }
        Repartidor repartidorAsignado = repartidores.get(0); // Mejorar esta lógica en el futuro

        // Crear y guardar el servicio de entrega
        ServicioEntrega servicioEntrega = new ServicioEntrega();
        servicioEntrega.setPedido(pedido);
        servicioEntrega.setDireccion(direccion);
        servicioEntrega.setRepartidor(repartidorAsignado);
        servicioEntrega.setFechaRecepcion(LocalDateTime.now());
        servicioEntrega.setFechaEntrega(null);
        servicioEntregaDAO.save(servicioEntrega);

        // Limpiar la sesión
        session.removeAttribute("carrito");
        session.removeAttribute("pedido");
        session.removeAttribute("direccionId");
        session.removeAttribute("metodoPago");
        session.removeAttribute("restauranteId");

        model.addAttribute("success", "Pedido confirmado y pago realizado con éxito.");
        return "pagoExitoso";
    }
}
