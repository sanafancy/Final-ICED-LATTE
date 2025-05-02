package proyecto.iso2.dominio.gestores;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import proyecto.iso2.dominio.entidades.*;
import proyecto.iso2.persistencia.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/restaurante")
public class RestauranteController {

    @Autowired
    private RestauranteDAO restauranteDAO;
    @Autowired
    private CartaMenuDAO cartaMenuDAO;
    @Autowired
    private ItemMenuDAO itemMenuDAO;
    @Autowired
    private UsuarioDAO usuarioDAO;

    /**
     * Home público: lista todos los restaurantes (cliente)
     */
    @GetMapping("/home")
    public String homeCliente(HttpSession session, Model model) {
        List<Restaurante> restaurantes = restauranteDAO.findAll();
        model.addAttribute("restaurantes", restaurantes);

        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("cliente", cliente);
        }
        return "inicio";
    }

    /**
     * Búsqueda de restaurantes
     */
    @GetMapping("/buscar")
    public String buscarRestaurante(@RequestParam(required = false) String busqueda,
                                    HttpSession session,
                                    Model model) {
        Set<Restaurante> resultados = new LinkedHashSet<>();

        if (busqueda != null && !busqueda.isBlank()) {
            resultados.addAll(restauranteDAO.findByNombreContainingIgnoreCase(busqueda));
            resultados.addAll(restauranteDAO.findByDireccion_CalleContainingIgnoreCase(busqueda));
            resultados.addAll(restauranteDAO.findByDireccion_MunicipioContainingIgnoreCase(busqueda));
            try {
                int cp = Integer.parseInt(busqueda);
                resultados.addAll(restauranteDAO.findByDireccion_CodigoPostal(cp));
            } catch (NumberFormatException ignored) {}
        } else {
            resultados.addAll(restauranteDAO.findAll());
        }

        model.addAttribute("restaurantes", List.copyOf(resultados));
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("cliente", cliente);
        }
        return "inicio";
    }

    /**
     * Página principal del restaurante autenticado
     */
    @GetMapping("/panel")
    public String inicioRestaurante(HttpSession session, Model model) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        if (restaurante == null) {
            return "redirect:/login";
        }

        model.addAttribute("restaurante", restaurante);

        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);
        for (CartaMenu carta : cartas) {
            carta.setItems(itemMenuDAO.findByCartaMenu(carta));
        }
        model.addAttribute("cartas", cartas);

        return "inicioRestaurante";
    }

    /**
     * Redirige desde /inicioRestaurante al panel (evita error 404)
     */
    @GetMapping("/inicioRestaurante")
    public String redirigirPanel() {
        return "redirect:/restaurante/panel";
    }

    /**
     * Alternar favorito (cliente)
     */
    @PostMapping("/favorito/{id}")
    public String toggleFavorito(@PathVariable Long id, HttpSession session) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            Optional<Restaurante> opt = restauranteDAO.findById(id);
            if (opt.isPresent()) {
                Restaurante r = opt.get();
                if (cliente.getFavoritos().contains(r)) {
                    cliente.getFavoritos().remove(r);
                } else {
                    cliente.getFavoritos().add(r);
                }
                usuarioDAO.save(cliente);
            }
        }
        return "redirect:/restaurante/home";
    }

    /**
     * Ver favoritos del cliente
     */
    @GetMapping("/favoritos")
    public String verFavoritos(HttpSession session, Model model) {
        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente == null) {
            return "redirect:/login";
        }
        model.addAttribute("favoritos", cliente.getFavoritos());
        return "favoritos";
    }

    /**
     * Eliminar restaurante autenticado
     */
    @PostMapping("/eliminar")
    public String eliminarRestaurante(HttpSession session) {
        Restaurante restaurante = (Restaurante) session.getAttribute("restaurante");
        if (restaurante != null) {
            restauranteDAO.delete(restaurante);
            session.invalidate();
        }
        return "redirect:/inicio";
    }

    /**
     * Ver menús públicos de un restaurante
     */
    @GetMapping("/{id}")
    public String verMenuRestaurante(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Restaurante> opt = restauranteDAO.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/restaurante/home";
        }
        Restaurante restaurante = opt.get();
        model.addAttribute("restaurante", restaurante);

        List<CartaMenu> cartas = cartaMenuDAO.findByRestaurante(restaurante);
        for (CartaMenu c : cartas) {
            c.setItems(itemMenuDAO.findByCartaMenu(c));
        }
        model.addAttribute("cartas", cartas);

        Cliente cliente = (Cliente) session.getAttribute("cliente");
        if (cliente != null) {
            model.addAttribute("direcciones", cliente.getDirecciones());
            model.addAttribute("cliente", cliente);
        }
        return "verMenus";
    }
}
