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

    @PostMapping("/pedido/finalizar")
    public String finalizarPedido(@RequestParam Long direccionId,
                                  @RequestParam Long pedidoId,
                                  @RequestParam MetodoPago metodoPago,
                                  HttpSession session,
                                  Model model){
        System.out.println("Entrando a metodo finalizarPedido");
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) return "redirect:/login";

        Optional<Pedido> pedidoOpt = pedidoDAO.findById(pedidoId);
        Optional<Direccion> direccionOpt = direccionDAO.findById(direccionId);
        if (!pedidoOpt.isPresent() || !direccionOpt.isPresent()) {
            model.addAttribute("error", "Datos inválidos");
            return "confirmarPedido";
        }

        Pedido pedido = pedidoOpt.get();
        Direccion direccion = direccionOpt.get();

        // Crear el pago
        Pago pago = new Pago();
        pago.setMetodoPago(metodoPago);
        pago.setPedido(pedido);
        pagoDAO.save(pago);

        pedido.setEstado(EstadoPedido.PAGADO);
        System.out.println("Estado del pedido en finalizarPedido(): "+pedido.getEstado());
        pedidoDAO.save(pedido);

        System.out.println("Entrando a apartado para asignar repartidor:");
        // Seleccionar un repartidor disponible (esto es una lógica muy simple)
        List<Repartidor> repartidores = repartidorDAO.findAll();
        if (repartidores.isEmpty()) {
            model.addAttribute("error", "Repartidor no encontrado");
            return "redirect:/pedido/confirmarPedido";
        }
        Repartidor repartidorAsignado = repartidores.get(0); // Aquí podrías aplicar lógica más avanzada

        System.out.println("Apunto de crear servicioEntrega");
        // Crear y guardar el servicio de entrega
        ServicioEntrega servicioEntrega = new ServicioEntrega();
        servicioEntrega.setPedido(pedido);
        servicioEntrega.setDireccion(direccion);
        servicioEntrega.setRepartidor(repartidorAsignado);
        servicioEntrega.setFechaRecepcion(LocalDateTime.now());
        servicioEntrega.setFechaEntrega(null);

        servicioEntregaDAO.save(servicioEntrega);

        model.addAttribute("success", "Repartidor asignado exitosamente.");
        return "redirect:/pagoExitoso"; // o redirige donde sea necesario
    }

}
