package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Transactional
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

        List<ItemMenu> items = new ArrayList<>();
        double total = 0;
        for (Map.Entry<Long, Integer> entry : carrito.entrySet()) {
            Optional<ItemMenu> itemOpt = itemMenuDAO.findById(entry.getKey());
            if (itemOpt.isPresent()) {
                ItemMenu item = itemOpt.get();
                items.add(item);
                total += item.getPrecio() * entry.getValue();
            }
        }
        pedido.setItems(items);
        pedido.setFecha(LocalDateTime.now());
        pedido.setTotal(total);
        pedido.setMetodoPago(metodoPagoEnum);

        try {
            // Crear y guardar el pago
            System.out.println("Creando pago para pedido ID: " + pedidoId);
            Pago pago = new Pago(metodoPagoEnum, pedido, LocalDateTime.now());
            System.out.println("Pago antes de guardar: idTransaccion=" + pago.getIdTransaccion() +
                    ", metodoPago=" + pago.getMetodoPago() +
                    ", pedidoId=" + (pago.getPedido() != null ? pago.getPedido().getId() : "null") +
                    ", fechaTransaccion=" + pago.getFechaTransaccion());
            System.out.println("Guardando pago...");
            Pago savedPago = pagoDAO.save(pago);
            System.out.println("Pago guardado con ID: " + savedPago.getIdTransaccion());

            // Actualizar estado del pedido a PAGADO
            System.out.println("Actualizando estado del pedido ID: " + pedidoId + " a PAGADO");
            pedido.setEstado(EstadoPedido.PAGADO);
            pedido.setPago(savedPago);
            pedidoDAO.save(pedido);
            System.out.println("Pedido actualizado");

            // Asignar repartidor
            System.out.println("Buscando repartidor...");
            List<Repartidor> repartidores = repartidorDAO.findAll();
            Repartidor repartidorAsignado = null;
            for (Repartidor repartidor : repartidores) {
                if (repartidor.getEficiencia() != null) {
                    repartidorAsignado = repartidor;
                    break;
                }
            }
            if (repartidorAsignado == null) {
                model.addAttribute("error", "No hay repartidores disponibles con eficiencia definida");
                return "confirmarPedido";
            }
            System.out.println("Repartidor asignado: " + repartidorAsignado.getNombre());

            // Crear y guardar el servicio de entrega
            System.out.println("Creando servicio de entrega...");
            ServicioEntrega servicioEntrega = new ServicioEntrega();
            servicioEntrega.setPedido(pedido);
            servicioEntrega.setDireccion(direccion);
            servicioEntrega.setRepartidor(repartidorAsignado);
            servicioEntrega.setFechaRecepcion(LocalDateTime.now());
            servicioEntrega.setFechaEntrega(null);
            servicioEntregaDAO.save(servicioEntrega);
            System.out.println("Servicio de entrega guardado");

            // Limpiar la sesión
            session.removeAttribute("carrito");
            session.removeAttribute("pedido");
            session.removeAttribute("direccionId");
            session.removeAttribute("metodoPago");
            session.removeAttribute("restauranteId");

            model.addAttribute("success", "Pedido confirmado y pago realizado con éxito.");
            return "pagoExitoso"; // Redirige a pagoExitoso.html
        } catch (Exception e) {
            System.err.println("Error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "confirmarPedido";
        }
    }
}