package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/repartidor")
public class RepartidorController {

    @Autowired
    private RepartidorDAO repartidorDAO;
    @Autowired
    private ServicioEntregaDAO servicioEntregaDAO;
    @Autowired
    private PedidoDAO pedidoDAO;

    @GetMapping("/seleccionar-codigo-postal")
    public String mostrarFormularioCodigoPostal(HttpSession session, Model model) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) {
            return "redirect:/login";
        }
        model.addAttribute("codigosPostales", CodigoPostal.values());
        model.addAttribute("repartidor", repartidor);
        return "seleccionarCodigoPostal";
    }

    @PostMapping("/seleccionar-codigo-postal")
    public String guardarCodigoPostal(@RequestParam String codigoPostal, HttpSession session) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) {
            return "redirect:/login";
        }
        try {
            int ordinal = Integer.parseInt(codigoPostal);
            CodigoPostal cp = CodigoPostal.values()[ordinal];
            repartidor.setCodigoPostal(cp);
            repartidorDAO.save(repartidor);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return "redirect:/repartidor/seleccionar-codigo-postal?error=invalid";
        }
        return "redirect:/repartidor/InicioRepartidor";
    }

    @GetMapping("/InicioRepartidor")
    public String mostrarInicioRepartidor(HttpSession session, Model model) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) {
            return "redirect:/login";
        }
        List<ServicioEntrega> serviciosEntrega = servicioEntregaDAO.findByRepartidor_IdUsuario(repartidor.getIdUsuario());
        model.addAttribute("repartidor", repartidor);
        model.addAttribute("serviciosEntrega", serviciosEntrega);
        return "InicioRepartidor";
    }

    @PostMapping("/marcar-recogido")
    public String marcarRecogido(@RequestParam Long servicioId, HttpSession session) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) return "redirect:/login";

        Optional<ServicioEntrega> servicioOpt = servicioEntregaDAO.findById(servicioId);
        if (servicioOpt.isPresent()) {
            ServicioEntrega servicio = servicioOpt.get();
            if (servicio.getRepartidor().getIdUsuario().equals(repartidor.getIdUsuario())) {
                servicio.setFechaRecepcion(LocalDateTime.now()); //solo cuando el repartidor lo marca
                servicioEntregaDAO.save(servicio);
                System.out.println("marcado como entregado");
                Pedido pedido = servicio.getPedido();
                pedido.setEstado(EstadoPedido.RECOGIDO);
                pedidoDAO.save(pedido);
            }
        }
        return "redirect:/repartidor/InicioRepartidor";
    }

    @PostMapping("/marcar-entregado")
    public String marcarEntregado(@RequestParam Long servicioId, HttpSession session) {
        Repartidor repartidor = (Repartidor) session.getAttribute("repartidor");
        if (repartidor == null) return "redirect:/login";

        Optional<ServicioEntrega> servicioOpt = servicioEntregaDAO.findById(servicioId);
        if (servicioOpt.isPresent()) {
            ServicioEntrega servicio = servicioOpt.get();
            if (servicio.getRepartidor().getIdUsuario().equals(repartidor.getIdUsuario())
                    && servicio.getFechaRecepcion() != null) {
                servicio.setFechaEntrega(LocalDateTime.now());
                servicioEntregaDAO.save(servicio);

                Pedido pedido = servicio.getPedido();
                pedido.setEstado(EstadoPedido.ENTREGADO);
                pedidoDAO.save(pedido);
            }
        }
        return "redirect:/repartidor/InicioRepartidor";
    }

}