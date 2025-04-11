package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;
import java.util.List;
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

    @PostMapping("/finalizar")
    public String finalizarPedido(@PathVariable Long pedidoId, HttpSession session, RedirectAttributes redirectAttrs) {
        Optional<Pedido> pedidoOpt = pedidoDAO.findById(pedidoId);

        if (!pedidoOpt.isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Pedido no encontrado.");
            return "redirect:/pedidos";
        }

        Pedido pedido = pedidoOpt.get();

        // Obtener dirección desde sesión (la que el cliente seleccionó)
        Long direccionIdSeleccionada = (Long) session.getAttribute("direccionSeleccionada");
        if (direccionIdSeleccionada == null) {
            redirectAttrs.addFlashAttribute("error", "No hay dirección seleccionada.");
            return "redirect:/pedido/confirmarPedido";
        }

        Optional<Direccion> direccionOpt = direccionDAO.findById(direccionIdSeleccionada);
        if (!direccionOpt.isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Dirección inválida.");
            return "redirect:/pedido/confirmarPedido";
        }
        Direccion direccion = direccionOpt.get();
        pedido.setEstado(EstadoPedido.PAGADO);
        pedidoDAO.save(pedido);

        // Seleccionar un repartidor disponible (esto es una lógica muy simple)
        List<Repartidor> repartidores = repartidorDAO.findAll();
        if (repartidores.isEmpty()) {
            redirectAttrs.addFlashAttribute("error", "No hay repartidores disponibles.");
            return "redirect:/pedido/confirmarPedido";
        }

        Repartidor repartidorAsignado = repartidores.get(0); // Aquí podrías aplicar lógica más avanzada

        // Crear y guardar el servicio de entrega
        ServicioEntrega servicioEntrega = new ServicioEntrega();
        servicioEntrega.setPedido(pedido);
        servicioEntrega.setDireccion(direccionOpt.get());
        servicioEntrega.setRepartidor(repartidorAsignado);
        servicioEntrega.setFechaRecepcion(LocalDateTime.now());
        servicioEntrega.setFechaEntrega(null);

        servicioEntregaDAO.save(servicioEntrega);

        redirectAttrs.addFlashAttribute("success", "Repartidor asignado exitosamente.");
        return "redirect:/pagoExitoso"; // o redirige donde sea necesario
    }

}
