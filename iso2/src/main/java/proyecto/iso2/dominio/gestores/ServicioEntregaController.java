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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ServicioEntregaController {
    private static final Logger logger = LoggerFactory.getLogger(ServicioEntregaController.class);
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

        // Validar metodo de pago
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

            List<ServicioEntrega> serviciosActivos = servicioEntregaDAO.findByDireccionAndFechaEntregaIsNull(direccion);
            if (!serviciosActivos.isEmpty()) {
                model.addAttribute("error", "Ya existe un pedido activo para esta dirección. Finalízalo antes de crear uno nuevo.");
                System.out.println("servicio ya activo para esta direccion.");
                return "confirmarPedido";
            }

            // Asignar repartidor
            System.out.println("Buscando repartidor...");
            Optional<Repartidor> repartidorOpt = asignarRepartidor(direccion, pedido);
            //comprobar restaurante en la zona
            if (direccion.getCodigoPostal()!=pedido.getRestaurante().getDireccion().getCodigoPostal()) {
                pedido.setEstado(EstadoPedido.PEDIDO);
                pedidoDAO.save(pedido);
                pago.setMetodoPago(null);
                pago.setPedido(null);
                pagoDAO.save(pago);
                model.addAttribute("error", "El restaurante y el cliente no están en la misma zona. No se puede asignar un repartidor.");
                return "confirmarPedido";
            }
            if (repartidorOpt.isEmpty()) { //error no hay repartidor
                model.addAttribute("error", "No hay repartidores disponibles en esta zona.");
                return "confirmarPedido";
            }
            Repartidor repartidorAsignado = repartidorOpt.get();
            System.out.println("Repartidor asignado: " + repartidorAsignado.getNombre());

            // Crear y guardar el servicio de entrega
            System.out.println("Creando servicio de entrega...");
            ServicioEntrega servicioEntrega = new ServicioEntrega();
            servicioEntrega.setPedido(pedido);
            servicioEntrega.setDireccion(direccion);
            servicioEntrega.setRepartidor(repartidorAsignado);
            servicioEntrega.setFechaRecepcion(null);
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
            logger.error("Error al procesar el pago", e);  // Corrección según SonarCloud
            model.addAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "confirmarPedido";
        }
    }

    private Optional<Repartidor> asignarRepartidor(Direccion direccionCliente, Pedido pedido) {
        Direccion direccionRestaurante = pedido.getRestaurante().getDireccion();
        // restaurante y cliente en el mismo cp
        if (direccionCliente.getCodigoPostal()!=direccionRestaurante.getCodigoPostal()) {
            return Optional.empty();
        }

        // buscar repartidores disponibles
        CodigoPostal codigo = CodigoPostal.fromInt(direccionCliente.getCodigoPostal());
        List<Repartidor> repartidoresZona = repartidorDAO.findByCodigoPostalOrderByEficienciaAsc(codigo);
        if (repartidoresZona.isEmpty()) {
            return Optional.empty();
        }

        Repartidor repartidor = repartidoresZona.get(0);
        repartidor.incrementarEficiencia();
        repartidorDAO.save(repartidor);
        return Optional.of(repartidor);

    }

}